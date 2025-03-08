import { WebPlugin } from '@capacitor/core';

import type { frpPlugin } from './definitions';

export class frpWeb extends WebPlugin implements frpPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
