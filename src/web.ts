import { WebPlugin } from '@capacitor/core';

import type { frpPlugin } from './definitions';
import * as console from "node:console";

export class frpWeb extends WebPlugin implements frpPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  startFrpc(): Promise<{ value: string }> {
    return Promise.reject({value: "FrpPlugin is only available on Android!"}); // @ts-ignore
  }
}
