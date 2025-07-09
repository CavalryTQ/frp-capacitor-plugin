import { WebPlugin } from '@capacitor/core';

import type { frpPlugin } from './definitions';

const text : string = "FrpPlugin is only available on Android!";
export class frpWeb extends WebPlugin implements frpPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

 async startFrpc(options:{filePath: string}): Promise<{ value: string }> {
    return Promise.reject({value: text,  filePath: options}); // @ts-ignore
  }

  testStartFrpc(): Promise<{ value: string }> {
    return Promise.resolve({value: text});
  }

  startDummyVpn(): Promise<{ value: string }> {
    return Promise.resolve({value: text});
  }

  isBatteryOptimizationIgnored(): Promise<{ value: boolean }> {
    return Promise.resolve({value: false});
  }

  requestIgnoreBatteryOptimizations(): Promise<{ value: boolean }> {
    return Promise.resolve({value: true});
  }

  requestVpnPermission(): Promise<{ value: string }> {
    return Promise.resolve({value: text});
  }

  requestNotificationPermission(): Promise<{ value: string }> {
    return Promise.resolve({value: text});
  }

  openAppSettings(): Promise<{ value: string }> {
    return Promise.resolve({value: text});
  }

  stopFrpc(): Promise<{ value: string }> {
    return Promise.resolve({value: text});
  }

  stopDummyVpn(): Promise<{ value: string }> {
    return Promise.resolve({value: text});
  }

  async getStatus(options: { mode: string }): Promise<{ value: string }> {
    return Promise.resolve({value: text, options: options});
  }

}
