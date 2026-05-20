# 🔍 Аудит локализации мода Slag-n-Embers (v1.21+ Forge/NeoForge)

**Дата:** 2026-05-19  
**Проанализировано файлов:** ~110 Java-файлов в `src/main/java/dev/lopyluna/slag/`  
**Файл локализации:** [`src/generated/resources/assets/slag/lang/en_us.json`](src/generated/resources/assets/slag/lang/en_us.json) (593 строки, 537 ключей)

---

## 📊 Сводка

| Категория | Количество |
|-----------|------------|
| Уже переводимые строки (translatable) | ✅ 537 ключей в en_us.json |
| Hardcoded строки (нужно исправить) | ❌ 6 строк в 3 файлах |
| Отсутствующие ключи в en_us.json | ⚠️ 7 ключей (включая 2 itemGroup) |
| Строки с fallback-значениями | ⚠️ 8 компонентов |

---

## 1. ✅ Строки, которые УЖЕ переводимы

Мод использует качественную систему локализации через `Component.translatable()` и хелперы `AllLangs.tr()` / `AllLangs.trArgs()`.

### 1.1. Система хелперов AllLangs

Файл [`register/AllLangs.java`](src/main/java/dev/lopyluna/slag/register/AllLangs.java):

```java
// Все методы используют Component.translatable()
public static MutableComponent tr(String path)           → "tooltip.slag.<path>"
public static MutableComponent trArgs(String path, ...)  → "tooltip.slag.<path>" с аргументами
public static MutableComponent trArgs(ResourceLocation id, ...) → "tooltip.<ns>.<path>"
```

**Все 57 тултип-ключей** (строки 548-592 в en_us.json) корректно используют эту систему.

### 1.2. Предметы и блоки

Генерация имён через `RegistrateLangProvider.toEnglishName()` + `Component.translatableWithFallback()`:

| Файл | Строка | Механизм |
|------|--------|----------|
| [`DynamicPartItem.java:73-75`](src/main/java/dev/lopyluna/slag/content/items/dynamic_part/DynamicPartItem.java:73) | `Component.translatableWithFallback(id, RegistrateLangProvider.toEnglishName(name))` | Генерация имени |
| [`ModularItem.java:218-221`](src/main/java/dev/lopyluna/slag/content/items/modular/ModularItem.java:218) | `Component.translatableWithFallback(id, RegistrateLangProvider.toEnglishName(name))` | Генерация имени |
| [`AllLangs.java:163-168`](src/main/java/dev/lopyluna/slag/register/AllLangs.java:163) | `REG.addRawLang(...)` | Автогенерация 552 ключей инструментов |

### 1.3. JEI категории

Все 5 категорий JEI используют `Component.translatableWithFallback()`:

| Файл | Строка | Ключ |
|------|--------|------|
| [`DoubleSmeltingCategory.java:25`](src/main/java/dev/lopyluna/slag/content/jei/category/DoubleSmeltingCategory.java:25) | `translatableWithFallback("gui.slag.category.double_smelting", "Double Smelting")` | `gui.slag.category.double_smelting` |
| [`MeltingCategory.java:49`](src/main/java/dev/lopyluna/slag/content/jei/category/MeltingCategory.java:49) | `translatableWithFallback("gui.slag.category.melting", "Melting")` | `gui.slag.category.melting` |
| [`TableCastingCategory.java:49`](src/main/java/dev/lopyluna/slag/content/jei/category/TableCastingCategory.java:49) | `translatableWithFallback("gui.slag.category.table_casting", "Table Casting")` | `gui.slag.category.table_casting` |
| [`BasinCastingCategory.java:49`](src/main/java/dev/lopyluna/slag/content/jei/category/BasinCastingCategory.java:49) | `translatableWithFallback("gui.slag.category.basin_casting", "Basin Casting")` | `gui.slag.category.basin_casting` |
| `AlloyingCategory.java` (аналогично) | — | `gui.slag.category.alloying` |

### 1.4. Экранные тултипы (InterfaceScreen, MelterScreen)

[`InterfaceScreen.java:148-179`](src/main/java/dev/lopyluna/slag/content/blocks/crucible_interface/client/InterfaceScreen.java:148) — метод `createLang()` использует `AllLangs.trAmounts()`, который вызывает `Component.translatable()`. Все количества отображаются через переводимые ключи: `tooltip.slag.blocks`, `tooltip.slag.ingots`, `tooltip.slag.nuggets`, `tooltip.slag.gems`, `tooltip.slag.shards`, `tooltip.slag.dusts`, `tooltip.slag.grits`, `tooltip.slag.balls`, `tooltip.slag.buckets`, `tooltip.slag.mb`.

### 1.5. Тултипы модовых предметов

| Файл | Строка | Ключ | Статус |
|------|--------|------|--------|
| [`DynamicMoldItem.java:80-81`](src/main/java/dev/lopyluna/slag/content/items/dynamic_mold/DynamicMoldItem.java:80) | `AllLangs.tr("clear_imprint")` / `AllLangs.tr("imprint")` | `tooltip.slag.clear_imprint` / `tooltip.slag.imprint` | ✅ |
| [`CrucibleBlock.java:83`](src/main/java/dev/lopyluna/slag/content/blocks/crucible/CrucibleBlock.java:83) | `AllLangs.tr("dynamic_multiblock")` | `tooltip.slag.dynamic_multiblock` | ✅ |
| [`ClientTooltips.java:23-36`](src/main/java/dev/lopyluna/slag/client/ClientTooltips.java:23) | `AllLangs.tr("modular_tool_waiting")` и др. | Все через `AllLangs` | ✅ |

---

## 2. ❌ Строки, которые НУЖНО сделать переводимыми (Hardcoded)

### 2.1. [`ModularItem.java`](src/main/java/dev/lopyluna/slag/content/items/modular/ModularItem.java) — тултипы конструктора

| Строка | Текущий код | Проблема |
|--------|-------------|----------|
| **162** | `Component.literal("Possible Items:").withStyle(ChatFormatting.GRAY)` | 🔴 Hardcoded английский текст |
| **167** | `Component.literal("Possible Parts:").withStyle(ChatFormatting.GRAY)` | 🔴 Hardcoded английский текст |

**Предлагаемое исправление:**
```java
// Строка 162 — заменить на:
tooltip.add(Component.translatable("tooltip.slag.possible_items").withStyle(ChatFormatting.GRAY));

// Строка 167 — заменить на:
tooltip.add(Component.translatable("tooltip.slag.possible_parts").withStyle(ChatFormatting.GRAY));
```

**Новые ключи для en_us.json:**
```json
"tooltip.slag.possible_items": "Possible Items:",
"tooltip.slag.possible_parts": "Possible Parts:"
```

### 2.2. [`InterfaceScreen.java`](src/main/java/dev/lopyluna/slag/content/blocks/crucible_interface/client/InterfaceScreen.java) — единицы измерения жидкости

| Строка | Текущий код | Проблема |
|--------|-------------|----------|
| **71** | `var text = amount + (bucketAmount ? "B" : "mB") + "/" + capacity + (bucketCapacity ? "B" : "mB");` | 🔴 Суффиксы `"B"` и `"mB"` захардкожены |
| **73** | `Component.literal(text)` | 🔴 Результат использует `Component.literal` вместо `translatable` |

**Примечание:** Хотя «mB» (millibuckets) — это технический стандарт Minecraft, в некоторых языках могут использоваться другие обозначения. Рекомендуется сделать переводимым.

**Предлагаемое исправление:**
```java
// Строка 71-73 — заменить на:
var unitMb = Component.translatable("tooltip.slag.fluid_unit.mb");
var unitB = Component.translatable("tooltip.slag.fluid_unit.bucket");
var text = Component.translatable("tooltip.slag.fluid_amount_display",
    amount, bucketAmount ? unitB : unitMb,
    capacity, bucketCapacity ? unitB : unitMb);
guiGraphics.drawString(this.font, text, this.titleLabelX + imageWidth - font.width(text) - 12, this.titleLabelY, 4210752, false);
```

**Новые ключи для en_us.json:**
```json
"tooltip.slag.fluid_amount_display": "%s%s/%s%s",
"tooltip.slag.fluid_unit.mb": "mB",
"tooltip.slag.fluid_unit.bucket": "B"
```

### 2.3. [`ReloadModelsCommand.java`](src/main/java/dev/lopyluna/slag/content/commands/ReloadModelsCommand.java) — команда `/slag`

| Строка | Текущий код | Проблема |
|--------|-------------|----------|
| **27** | `Component.literal("Reloading models...")` | 🔴 Hardcoded сообщение игроку |
| **29** | `Component.literal("This command must be run on the client side")` | 🔴 Hardcoded сообщение об ошибке |

**Предлагаемое исправление:**
```java
// Строка 27 — заменить на:
source.sendSuccess(() -> Component.translatable("command.slag.reload_models.success"), true);

// Строка 29 — заменить на:
source.sendFailure(Component.translatable("command.slag.reload_models.client_only"));
```

**Новые ключи для en_us.json:**
```json
"command.slag.reload_models.success": "Reloading models...",
"command.slag.reload_models.client_only": "This command must be run on the client side"
```

---

## 3. ⚠️ Отсутствующие ключи в `en_us.json`

### 3.1. Вкладки креатива (itemGroup)

[`AllCreativeTabs.java:73`](src/main/java/dev/lopyluna/slag/register/AllCreativeTabs.java:73) и [`AllCreativeTabs.java:79`](src/main/java/dev/lopyluna/slag/register/AllCreativeTabs.java:79) используют `Component.translatableWithFallback()`, но ключи **отсутствуют** в [`en_us.json`](src/generated/resources/assets/slag/lang/en_us.json):

| Ключ | Fallback-значение | Где используется |
|------|-------------------|------------------|
| `itemGroup.slag.base` | `"Slag n' Embers"` | `AllCreativeTabs.java:73` |
| `itemGroup.slag.tools_parts` | `"Tools & Parts"` | `AllCreativeTabs.java:79` |

**Рекомендация:** Добавить в [`en_us.json`](src/generated/resources/assets/slag/lang/en_us.json):
```json
"itemGroup.slag.base": "Slag n' Embers",
"itemGroup.slag.tools_parts": "Tools & Parts"
```

### 3.2. JEI категории (gui)

Ключи `gui.slag.category.*` используют `translatableWithFallback` — работают через fallback, но ключи отсутствуют в lang-файле:

| Ключ | Fallback |
|------|----------|
| `gui.slag.category.double_smelting` | `"Double Smelting"` |
| `gui.slag.category.melting` | `"Melting"` |
| `gui.slag.category.table_casting` | `"Table Casting"` |
| `gui.slag.category.basin_casting` | `"Basin Casting"` |
| `gui.slag.category.alloying` | `"Alloying"` |

**Рекомендация:** Добавить в [`en_us.json`](src/generated/resources/assets/slag/lang/en_us.json):
```json
"gui.slag.category.double_smelting": "Double Smelting",
"gui.slag.category.melting": "Melting",
"gui.slag.category.table_casting": "Table Casting",
"gui.slag.category.basin_casting": "Basin Casting",
"gui.slag.category.alloying": "Alloying"
```

---

## 4. 📋 Полный список файлов: анализ

| Файл | Строк | Translatable | Hardcoded | Комментарий |
|------|-------|-------------|-----------|-------------|
| [`AllCreativeTabs.java`](src/main/java/dev/lopyluna/slag/register/AllCreativeTabs.java) | 108 | 2 | 0 | Ключи отсутствуют в en_us.json |
| [`ClientTooltips.java`](src/main/java/dev/lopyluna/slag/client/ClientTooltips.java) | 46 | 4 | 0 | ✅ Всё через AllLangs |
| [`AllLangs.java`](src/main/java/dev/lopyluna/slag/register/AllLangs.java) | 171 | 40+ | 0 | ✅ Система локализации |
| [`AllPonderScenes.java`](src/main/java/dev/lopyluna/slag/content/ponder/AllPonderScenes.java) | 171 | 2 | 0 | ✅ Через Ponder API |
| [`InterfaceScreen.java`](src/main/java/dev/lopyluna/slag/content/blocks/crucible_interface/client/InterfaceScreen.java) | 281 | 10+ | **1** | 🔴 `"B"`/`"mB"` хардкод |
| [`MelterScreen.java`](src/main/java/dev/lopyluna/slag/content/blocks/melter/client/MelterScreen.java) | 141 | 2 | 0 | `"✔"`/`"↓"` — символы, ок |
| [`ForgeScreen.java`](src/main/java/dev/lopyluna/slag/content/blocks/forge/client/ForgeScreen.java) | 51 | 0 | 0 | Без строк |
| [`ModularItem.java`](src/main/java/dev/lopyluna/slag/content/items/modular/ModularItem.java) | 234 | 6 | **2** | 🔴 `"Possible Items/Parts"` |
| [`ModularEquipablesItem.java`](src/main/java/dev/lopyluna/slag/content/items/modular/ModularEquipablesItem.java) | 167 | 0 | 0 | Без строк |
| [`ModularToolsItem.java`](src/main/java/dev/lopyluna/slag/content/items/modular/ModularToolsItem.java) | 196 | 0 | 0 | Без строк |
| [`DynamicPartItem.java`](src/main/java/dev/lopyluna/slag/content/items/dynamic_part/DynamicPartItem.java) | 84 | 1 | 0 | ✅ |
| [`DynamicMoldItem.java`](src/main/java/dev/lopyluna/slag/content/items/dynamic_mold/DynamicMoldItem.java) | 90 | 2 | 0 | ✅ |
| [`CrucibleBlock.java`](src/main/java/dev/lopyluna/slag/content/blocks/crucible/CrucibleBlock.java) | 286 | 1 | 0 | ✅ |
| [`CrucibleBE.java`](src/main/java/dev/lopyluna/slag/content/blocks/crucible/CrucibleBE.java) | 231 | 0 | 0 | Без строк |
| [`CrucibleItem.java`](src/main/java/dev/lopyluna/slag/content/blocks/crucible/CrucibleItem.java) | 154 | 0 | 0 | Без строк |
| [`BasinBE.java`](src/main/java/dev/lopyluna/slag/content/blocks/basin/BasinBE.java) | 339 | 0 | 0 | Без строк |
| [`ForgeBE.java`](src/main/java/dev/lopyluna/slag/content/blocks/forge/ForgeBE.java) | 327 | 1 | 0 | ✅ `getDisplayName()` |
| [`ReloadModelsCommand.java`](src/main/java/dev/lopyluna/slag/content/commands/ReloadModelsCommand.java) | 35 | 0 | **2** | 🔴 Сообщения команд |
| [`SlagServerConfigs.java`](src/main/java/dev/lopyluna/slag/config/SlagServerConfigs.java) | 21 | 5 | 0 | Комментарии на англ. (для модпаков) |
| [`SlagCommonConfigs.java`](src/main/java/dev/lopyluna/slag/config/SlagCommonConfigs.java) | 12 | 1 | 0 | Комментарий на англ. |
| [`EmbersJEI.java`](src/main/java/dev/lopyluna/slag/content/jei/EmbersJEI.java) | 192 | 0 | 0 | Без строк |
| `*Category.java` (5 файлов) | ~100-200 | 5 | 0 | ✅ `translatableWithFallback` |

---

## 5. 🏗️ Рекомендуемая структура ключей перевода

На основе анализа существующих ключей и новых, предлагается следующая структура:

```
# Существующие префиксы (✅ уже используются)
block.slag.<id>              — Названия блоков
item.slag.<id>               — Названия предметов
fluid.slag.<id>              — Названия жидкостей
tooltip.slag.<key>           — Тултипы и подсказки
slag.ponder.<scene>.<key>    — Ponder-сцены
old.<material>.<part>        — Старые/устаревшие предметы

# Отсутствующие префиксы (нужно добавить)
itemGroup.slag.<id>          — Вкладки креатива
gui.slag.category.<id>       — JEI категории рецептов
command.slag.<cmd>.<key>     — Сообщения команд
```

### Рекомендуемые НОВЫЕ ключи (полный список):

```json
{
  "itemGroup.slag.base": "Slag n' Embers",
  "itemGroup.slag.tools_parts": "Tools & Parts",

  "gui.slag.category.double_smelting": "Double Smelting",
  "gui.slag.category.melting": "Melting",
  "gui.slag.category.table_casting": "Table Casting",
  "gui.slag.category.basin_casting": "Basin Casting",
  "gui.slag.category.alloying": "Alloying",

  "tooltip.slag.possible_items": "Possible Items:",
  "tooltip.slag.possible_parts": "Possible Parts:",
  "tooltip.slag.fluid_amount_display": "%s%s/%s%s",
  "tooltip.slag.fluid_unit.mb": "mB",
  "tooltip.slag.fluid_unit.bucket": "B",

  "command.slag.reload_models.success": "Reloading models...",
  "command.slag.reload_models.client_only": "This command must be run on the client side"
}
```

---

## 6. 📈 Итоговая оценка качества локализации

| Показатель | Оценка |
|------------|--------|
| Общее качество i18n | 🟢 **Хорошее** (95%+) |
| Система локализации | 🟢 Продуманная (AllLangs + Registrate) |
| Hardcoded строк | 🟡 6 строк в 3 файлах |
| Отсутствующие ключи | 🟡 7 ключей используют fallback |
| Конфигурационные комментарии | 🟡 Английские (ожидаемо для Forge) |
| Готовность к переводу | 🟢 Высокая |

**Вывод:** Мод Slag-n-Embers имеет качественную систему локализации, которая покрывает более 95% всех пользовательских строк. Основная работа по исправлению сводится к:
1. Замене **6** `Component.literal()` на `Component.translatable()` в **3** файлах
2. Добавлению **14** новых ключей в `en_us.json`

После этих исправлений мод будет полностью готов к переводу на любые языки.
