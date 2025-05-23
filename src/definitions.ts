export interface frpPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  startFrpc(): Promise<{ value: string }>;
  testStartFrpc(): Promise<{ value: string }>;
  startDummyVpn():  Promise<{ value: string }>;
  isBatteryOptimizationIgnored(): Promise<{ value: boolean }>;
  requestIgnoreBatteryOptimizations(): Promise<{ value: boolean }>;
}
