# frp-plugin

Plugin provided frp for Android

## Install

```bash
npm install frp-plugin
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`startFrpc(...)`](#startfrpc)
* [`stopFrpc()`](#stopfrpc)
* [`testStartFrpc()`](#teststartfrpc)
* [`startDummyVpn()`](#startdummyvpn)
* [`stopDummyVpn()`](#stopdummyvpn)
* [`isBatteryOptimizationIgnored()`](#isbatteryoptimizationignored)
* [`requestIgnoreBatteryOptimizations()`](#requestignorebatteryoptimizations)
* [`requestVpnPermission(...)`](#requestvpnpermission)
* [`requestNotificationPermission(...)`](#requestnotificationpermission)
* [`openAppSettings()`](#openappsettings)
* [`addListener('frpOutput', ...)`](#addlistenerfrpoutput-)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### startFrpc(...)

```typescript
startFrpc(options: { filePath: string; }) => Promise<{ value: string; }>
```

| Param         | Type                               |
| ------------- | ---------------------------------- |
| **`options`** | <code>{ filePath: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### stopFrpc()

```typescript
stopFrpc() => Promise<{ value: string; }>
```

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### testStartFrpc()

```typescript
testStartFrpc() => Promise<{ value: string; }>
```

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### startDummyVpn()

```typescript
startDummyVpn() => Promise<{ value: string; }>
```

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### stopDummyVpn()

```typescript
stopDummyVpn() => Promise<{ value: string; }>
```

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### isBatteryOptimizationIgnored()

```typescript
isBatteryOptimizationIgnored() => Promise<{ value: boolean; }>
```

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

--------------------


### requestIgnoreBatteryOptimizations()

```typescript
requestIgnoreBatteryOptimizations() => Promise<{ value: boolean; }>
```

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

--------------------


### requestVpnPermission(...)

```typescript
requestVpnPermission(options: { value: boolean; }) => Promise<{ value: string; }>
```

| Param         | Type                             |
| ------------- | -------------------------------- |
| **`options`** | <code>{ value: boolean; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### requestNotificationPermission(...)

```typescript
requestNotificationPermission(options: { value: boolean; }) => Promise<{ value: string; }>
```

| Param         | Type                             |
| ------------- | -------------------------------- |
| **`options`** | <code>{ value: boolean; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### openAppSettings()

```typescript
openAppSettings() => Promise<{ value: string; }>
```

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### addListener('frpOutput', ...)

```typescript
addListener(event: 'frpOutput', callback: (data: { line: string; }) => void) => Promise<PluginListenerHandle>
```

| Param          | Type                                              |
| -------------- | ------------------------------------------------- |
| **`event`**    | <code>'frpOutput'</code>                          |
| **`callback`** | <code>(data: { line: string; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### Interfaces


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |

</docgen-api>
