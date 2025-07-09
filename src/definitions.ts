import {PluginListenerHandle} from "@capacitor/core";


export interface frpPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  startFrpc(options:{filePath: string}): Promise<{ value: string }>;
  stopFrpc(): Promise<{ value: string }>;
  getStatus(options:{mode: string}): Promise<{ value: string }>;
  testStartFrpc(): Promise<{ value: string }>;
  startDummyVpn():  Promise<{ value: string }>;
  stopDummyVpn():  Promise<{ value: string }>;
  isBatteryOptimizationIgnored(): Promise<{ value: boolean }>;
  requestIgnoreBatteryOptimizations(): Promise<{ value: boolean }>;
  requestVpnPermission(options:{value: boolean}):  Promise<{ value: string }>;
  requestNotificationPermission(options:{value: boolean}): Promise<{ value: string }>;
  openAppSettings():  Promise<{ value: string }>;


  addListener(
      event: 'frpOutput',
      callback: (data: { line: string }) => void
  ): Promise<PluginListenerHandle>;

  removeAllListeners(): Promise<void>;
}
