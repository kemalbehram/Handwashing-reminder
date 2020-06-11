/*
 * Copyright © 2020 - present | Handwashing reminder by Javinator9889
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 *
 * Created by Javinator9889 on 12/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities.views.fragments.news

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.BaseFragmentView
import com.javinator9889.handwashingreminder.activities.base.LayoutVisibilityChange
import com.javinator9889.handwashingreminder.activities.views.fragments.news.adapter.News
import com.javinator9889.handwashingreminder.activities.views.viewmodels.NewsViewModel
import com.javinator9889.handwashingreminder.data.UserProperties
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.adapters.GenericItemAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.fastadapter.scroll.EndlessRecyclerOnScrollListener
import com.mikepenz.fastadapter.ui.items.ProgressItem
import kotlinx.android.synthetic.main.loading_recycler_view.*
import kotlinx.android.synthetic.main.loading_recycler_view.view.*
import kotlinx.coroutines.launch
import timber.log.Timber

class NewsFragment : BaseFragmentView(), LayoutVisibilityChange {
    @LayoutRes
    override val layoutId: Int = R.layout.loading_recycler_view
    private lateinit var fastAdapter: FastAdapter<GenericItem>
    private lateinit var footerAdapter: GenericItemAdapter
    private val newsAdapter = ItemAdapter<News>()
    private val newsViewModel: NewsViewModel by viewModels()
    private val activeItems = mutableSetOf<String>()

    init {
        lifecycleScope.launch {
            whenStarted {
                loading.visibility = View.VISIBLE
                newsViewModel.newsData.observe(viewLifecycleOwner, Observer {
                    if (::footerAdapter.isInitialized)
                        footerAdapter.clear()
                    if (it.id !in activeItems) {
                        val newsObject = News(
                            title = it.title,
                            short = "${it.text.take(200)}…",
                            url = it.url,
                            discoverDate = it.discoverDate,
                            imageUrl = it.elements?.url,
                            website = it.website?.name,
                            websiteImageUrl = it.website?.iconURL,
                            lifecycleScope = this@NewsFragment.lifecycleScope
                        )
                        newsAdapter.add(newsObject)
                        loading.visibility = View.INVISIBLE
                        container.visibility = View.VISIBLE
                        activeItems.add(it.id)
                    }
                })
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        footerAdapter = ItemAdapter.items()
        fastAdapter = FastAdapter.with(listOf(newsAdapter, footerAdapter))
        val rvManager = LinearLayoutManager(context)
        val scrollListener =
            object : EndlessRecyclerOnScrollListener(footerAdapter) {
                override fun onLoadMore(currentPage: Int) {
                    view.container.post {
                        footerAdapter.clear()
                        Timber.d("Loading more")
                        val progressItem = ProgressItem()
                        progressItem.isEnabled = true
                        footerAdapter.add(progressItem)
                        lifecycleScope.launch {
                            newsViewModel.populateData(
                                from = newsAdapter.adapterItemCount,
                                amount = 20,
                                language = UserProperties.language
                            )
                        }
                    }
                }
            }
        with(view.container) {
            layoutManager = rvManager
            adapter = fastAdapter
            itemAnimator = DefaultItemAnimator()
            addOnScrollListener(scrollListener)
        }
        fastAdapter.addEventHooks(listOf(NewsClickHook(), ShareClickHook()))
        fastAdapter.withSavedInstanceState(savedInstanceState)
        if (savedInstanceState == null) {
            lifecycleScope.launch {
                newsViewModel.populateData(language = UserProperties.language)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fastAdapter.saveInstanceState(outState)
    }

    private inner class NewsClickHook : ClickEventHook<News>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder) =
            if (viewHolder is News.ViewHolder) viewHolder.cardContainer
            else null

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<News>,
            item: News
        ) {
            val website = Uri.parse(item.url)
            with(Intent(Intent.ACTION_VIEW, website)) {
                if (resolveActivity(requireContext().packageManager) != null)
                    startActivity(this)
                else {
                    MaterialDialog(requireContext()).show {
                        title(R.string.no_app)
                        message(
                            text = getString(
                                R.string.no_app_long,
                                getString(R.string.browser_err)
                            )
                        )
                        positiveButton(android.R.string.ok)
                        cancelable(true)
                        cancelOnTouchOutside(true)
                    }
                }
            }
        }
    }

    override fun onVisibilityChanged(visibility: Int) {
        TODO("Not yet implemented")
    }

    private inner class ShareClickHook : ClickEventHook<News>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder) =
            if (viewHolder is News.ViewHolder) viewHolder.shareImage
            else null

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<News>,
            item: News
        ) {
            with(Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT,
                    "${item.title} — ${item.url} via Handwashing Reminder"
                )
                putExtra(Intent.EXTRA_TITLE, item.title)
                item.imageUrl?.let { data = Uri.parse(it) }
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                type = "text/plain"
            }, null)) {
                startActivity(this)
            }
        }
    }
}