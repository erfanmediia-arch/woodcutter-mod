# 🪓 Woodcutter Bot Mod — Minecraft Java 1.20.1 (Forge)

## راهنمای نصب و استفاده (فارسی)

### پیش‌نیازها
- Minecraft Java Edition 1.20.1
- Forge 47.2.0+
- Java 17

### نحوه ساخت (Build)
1. [Forge MDK 1.20.1](https://files.minecraftforge.net/) را دانلود کنید
2. فایل‌های این پروژه را داخل پوشه MDK قرار دهید (جایگزین فایل‌های موجود)
3. دستور زیر را در ترمینال اجرا کنید:
   ```bash
   ./gradlew build
   ```
4. فایل `.jar` ساخته‌شده در پوشه `build/libs/` خواهد بود

### نصب ماد
1. فایل `woodcuttermod-1.0.0.jar` را به پوشه `mods/` ماینکرافت منتقل کنید
2. بازی را اجرا کنید (با Forge Profile)

### دستورها
| دستور | توضیح |
|-------|-------|
| `/spawnbot` | یک ربات woodcutter کنار شما اسپاون می‌شه |
| `/removebots` | همه ربات‌های اطراف رو حذف می‌کنه (فقط OP) |

---

## Features (English)

- Spawns a **Woodcutter Bot** NPC that automatically:
  - 🪵 Chops wood logs (Oak, Birch, Spruce, Jungle, Acacia, Dark Oak, Mangrove, Cherry)
  - 🍂 Breaks leaves
  - 🌿 Breaks grass blocks, short grass, tall grass, ferns
- Bot **navigates** to the nearest target block within 12 block radius
- Bot **animates** arm swings and shows **break progress**
- Drops loot naturally (items fall on the ground)
- Name tag shows current bot status

## File Structure
```
src/main/java/com/woodcutter/mod/
├── WoodcutterMod.java          ← Main mod class, entity registration
├── WoodcutterCommand.java      ← /spawnbot and /removebots commands
└── entity/
    └── WoodcutterBotEntity.java ← Bot AI logic (search, walk, break)

src/main/resources/
├── META-INF/mods.toml          ← Mod metadata
└── pack.mcmeta
```

## How the Bot AI Works
1. **SEARCHING** — Scans a 12-block radius for breakable blocks
2. **WALKING** — Pathfinds to the nearest target
3. **BREAKING** — Faces the block, swings axe, applies break progress
4. **IDLE** — Short pause after breaking, then searches again

## Customization (in WoodcutterBotEntity.java)
- `SEARCH_RADIUS` — how far bot looks for blocks (default: 12)
- `BREAK_INTERVAL` — speed of breaking (default: 25 ticks)
- `BREAKABLE_BLOCKS` list — add/remove block types the bot targets
