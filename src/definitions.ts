export interface frpPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
