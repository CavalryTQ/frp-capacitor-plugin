import { registerPlugin } from '@capacitor/core';

import type { frpPlugin } from './definitions';

const frp = registerPlugin<frpPlugin>('frp', {
  web: () => import('./web').then((m) => new m.frpWeb()),
});

export * from './definitions';
export { frp };
