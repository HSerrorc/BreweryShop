# BreweryShop

一个 BreweryX 酒类出售插件。玩家把酿好的酒放进 GUI，按配方与品质自动结算金币。

## 依赖

- [BreweryX](https://github.com/BreweryTeam/BreweryX) 3.7.0+
- [Vault](https://www.spigotmc.org/resources/vault.34315/) + 任意经济插件
- Paper / Spigot 1.21.4+

## 命令

| 命令 | 说明 |
|---|---|
| `/sellbrew` | 打开酒类出售 GUI |

## 配置

```yaml
prices:
  ia_drink_01-1: 1100    # 配方ID-品质: 价格
  ia_drink_01-10: 3300

gui:
  title: "酒类出售"
  layout:
    - "========="
    - "=       ="
    - "=       ="
    - "=       ="
    - "====S===="
```

**价格键格式**为 `配方ID-品质`，品质范围 1–10。

**GUI 布局**用字符画定义：`=` 是边框，空格是可放酒的槽位，`S` 是出售按钮。

## 说明

- 只收购**已封存**（sealed）的 BreweryX 酒品，未封存的不计价
- 放入物品后总价实时显示在出售按钮上
- 关闭 GUI 时，剩余物品会退回背包
- 价格未配置的酒按 0 计，可开 `debug: true` 查看结算日志

## 构建

```bash
mvn clean package
```

产物在 `target/BreweryShop-1.0.0.jar`。
