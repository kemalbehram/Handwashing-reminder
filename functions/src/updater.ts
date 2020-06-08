import {NewsriverData} from "./newsriver";
import * as firebaseHelper from 'firebase-functions-helper';
import * as fetch from 'node-fetch';
import {Headers} from "node-fetch";

export class Updater {
  private readonly db: FirebaseFirestore.Firestore;
  private readonly collectionName: string;
  private readonly interval: number;
  searchTerms: Array<string>;
  private readonly language: string;
  private readonly auth: string;
  private _url: string | undefined;

  get url(): Promise<string> {
    if (this._url === undefined)
      return this.buildURL()
        .then(url => this._url = url);
    return Promise.resolve(this._url);
  }

  get collection(): FirebaseFirestore.CollectionReference {
    return this.db.collection(this.collectionName);
  }

  constructor(db: FirebaseFirestore.Firestore | null,
              collectionName: string | null,
              searchTerms: Array<string>,
              auth: string,
              language: string = 'en',
              intervalMins: number = 60) {
    this.db = db;
    this.collectionName = collectionName;
    this.searchTerms = searchTerms;
    this.language = language;
    this.auth = auth;
    this.interval = intervalMins * 60 * 1000;
    this.request()
      .then(response => {
        this.updateData(response)
          .catch(ignored => {
          });
      })
  }

  schedule(): NodeJS.Timer {
    return setInterval(() => {
      const that = this;
      this.request()
        .then(response => {
          that.updateData(response)
            .catch(error => console.error(`error occurred while updating firebase data ${error}`));
        })
        .catch(error => console.error(`error occurred during process ${error}`));
    }, this.interval);
  }

  async updateData(content: Array<NewsriverData>) {
    console.log('Updating database content data');
    try {
      content.forEach(element => {
        firebaseHelper.firestore.checkDocumentExists(this.db, this.collectionName, element.id)
          .then(_ => firebaseHelper.firestore.createDocumentWithID(this.db, this.collectionName, element.id, element))
        console.log(`Created element with ID: ${element.id}`);
      });
    } catch (error) {
      throw error;
    }
  }

  async request(): Promise<Array<NewsriverData>> {
    try {
      const requestUrl = await this.url;
      const response = await fetch(requestUrl, {
        method: 'GET', headers: new Headers({
          'Authorization': this.auth,
          'Content-Type': 'application/json'
        })
      });
      const body = await response.json();
      return body as Array<NewsriverData>;
    } catch (e) {
      console.error(`Captured error ${e}`);
      throw e;
    }
  }

  buildURL(): Promise<string> {
    return new Promise(resolve => {
      const parts = ['https://api.newsriver.io/v2/search?query='];
      this.searchTerms.forEach((term, i, _) => {
        if (i !== 0)
          parts.push(encodeURI(' OR '));
        parts.push(encodeURI(`title:${term} OR text:${term}`));
      });
      let language: string;
      switch (this.language) {
        case 'es':
          language = 'ES';
          break;
        default:
          language = 'EN';
          break;
      }
      parts.push(encodeURI(` AND language:${language}`));
      parts.push(encodeURI('&sortBy=discoverDate'));
      parts.push(encodeURI('&sortOrder=DESC'));
      parts.push(encodeURI('&limit=10'));
      resolve(parts.join(''));
    });
  }
}