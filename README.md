# KMC Soulslike Regen

A Forge 1.20.1 mod that replaces Minecraft's vanilla natural regeneration with a **Soulslike fatigue system**. Players have a regeneration capacity (maxCap) that depletes as they heal. When the capacity is exhausted, natural healing stops completely—neither healing nor food is consumed (UHC-style). Capacity recovers only through specific in-world actions: resting at a Team Nexus, staying at an Inn, using a Waystone, sleeping in a bed, or resting by a campfire.

## Features

- **Soulslike Fatigue System**: Replace vanilla regen with a Soulslike-style capacity system
- **Team Nexus Zones**: Designated areas where team members drain fatigue instantly
- **Inn Zones**: Global recovery zones with warmup timer and gradual fatigue drain
- **Waystone Integration**: Full fatigue reset when using Waystones (Waystones mod)
- **Level-Up System**: Permanent capacity increases through cumulative fatigue spending
- **Campfire Rest**: Light fatigue recovery while stationary near lit campfires
- **Day Survival Bonus**: Bonus fatigue drain for going 24000 ticks without damage
- **Bed Sleep**: Daily fatigue reduction from sleeping
- **Ally Proximity Bonus**: Faster fatigue drain when near team members (FTB Teams integration)
- **Admin CRUD Commands**: Complete player stat manipulation system
- **ActionBar Status Display**: Optional persistent capacity bar display

## Installation

1. Install **Forge 47.x** for Minecraft 1.20.1
2. Place the mod JAR in your `mods/` folder
3. (Optional) Install **FTB Teams** mod for team features
4. (Optional) Install **Waystones** mod for Waystone integration
5. Launch the game

## Commands

All commands use either `/soulslikeregen` or the alias `/slregen`.

### Zone Management Commands (OP level 2 required)

#### Team Nexus Commands

**Set Team Nexus**
```
/slregen setTeamNexus <x> <y> <z> <radius> <teamName>
```
Create a new Team Nexus zone at the specified coordinates. Team members inside will drain fatigue at 1 unit/second.

Example:
```
/slregen setTeamNexus 100 64 200 25 "Alpha Team"
```

**Edit Team Nexus Radius**
```
/slregen editTeamNexus <id> radius <newRadius>
```
Modify the radius of an existing Nexus zone.

Example:
```
/slregen editTeamNexus 1 radius 50
```

**Edit Team Nexus Coordinates**
```
/slregen editTeamNexus <id> coords <x> <y> <z>
```
Move a Nexus zone to new coordinates.

Example:
```
/slregen editTeamNexus 1 coords 150 70 250
```

**Remove Team Nexus**
```
/slregen removeTeamNexus <id>
```
Delete a Nexus zone.

Example:
```
/slregen removeTeamNexus 1
```

**List All Team Nexuses**
```
/slregen listNexus [page]
```
View all configured Nexus zones (paginated, 5 per page).

Example:
```
/slregen listNexus 1
```

---

#### Inn Commands

**Set Inn**
```
/slregen setInn <x> <y> <z> <radius>
```
Create a new Inn zone (global recovery area). Players inside will drain fatigue at 1 unit/2 seconds after 1 minute warmup.

Example:
```
/slregen setInn 500 64 500 30
```

**Edit Inn Radius**
```
/slregen editInn <id> radius <newRadius>
```
Modify the radius of an existing Inn zone.

Example:
```
/slregen editInn 1 radius 40
```

**Edit Inn Coordinates**
```
/slregen editInn <id> coords <x> <y> <z>
```
Move an Inn zone to new coordinates.

Example:
```
/slregen editInn 1 coords 600 70 600
```

**Remove Inn**
```
/slregen removeInn <id>
```
Delete an Inn zone.

Example:
```
/slregen removeInn 1
```

**List All Inns**
```
/slregen listInns [page]
```
View all configured Inn zones (paginated, 5 per page).

Example:
```
/slregen listInns
```

---

### Player Stat Commands (OP level 2 required)

#### Fatigue Management

**Get Current Fatigue**
```
/slregen player <playerName> fatigue get
```
Display the player's current fatigue and max capacity.

Example:
```
/slregen player Steve fatigue get
```
Output: `[SLRegen] Steve's fatigue: 15.2 / 40.0`

**Set Fatigue**
```
/slregen player <playerName> fatigue set <amount>
```
Set the player's fatigue to a specific value (clamped to [0, maxCap]).

Example:
```
/slregen player Steve fatigue set 25.0
```

**Add Fatigue**
```
/slregen player <playerName> fatigue add <amount>
```
Increase the player's fatigue by the specified amount (auto-clamped to maxCap).

Example:
```
/slregen player Steve fatigue add 10.5
```

**Drain Fatigue**
```
/slregen player <playerName> fatigue drain <amount>
```
Decrease the player's fatigue by the specified amount.

Example:
```
/slregen player Steve fatigue drain 5.0
```

---

#### Capacity Management

**Get Max Capacity**
```
/slregen player <playerName> capacity get
```
Display the player's maximum capacity (maxCap).

Example:
```
/slregen player Steve capacity get
```
Output: `[SLRegen] Steve's max capacity: 40.0`

**Set Max Capacity**
```
/slregen player <playerName> capacity set <amount>
```
Set the player's maximum capacity to a specific value.

Example:
```
/slregen player Steve capacity set 100.0
```

**Reset Capacity to Base**
```
/slregen player <playerName> capacity reset
```
Reset the player's max capacity to the configured base value (default: 40.0).

Example:
```
/slregen player Steve capacity reset
```

---

#### Level Management

**Get Current Level**
```
/slregen player <playerName> level get
```
Display the player's current level.

Example:
```
/slregen player Steve level get
```
Output: `[SLRegen] Steve's level: 3`

**Set Level**
```
/slregen player <playerName> level set <level>
```
Set the player's level to a specific value (does NOT automatically update totalFatigueSpent).

Example:
```
/slregen player Steve level set 5
```

**Level Up**
```
/slregen player <playerName> level up [amount]
```
Increase the player's level by the specified amount (default: 1).

Example:
```
/slregen player Steve level up 2
```

**Reset Level**
```
/slregen player <playerName> level reset
```
Reset the player's level to 0.

Example:
```
/slregen player Steve level reset
```

---

#### Total Fatigue Tracking

**Get Total Fatigue Spent**
```
/slregen player <playerName> totalfatigue get
```
Display the cumulative fatigue the player has spent (for level-up progression tracking).

Example:
```
/slregen player Steve totalfatigue get
```
Output: `[SLRegen] Steve's total fatigue spent: 250.5`

---

#### Cooldown Management

**Reset Day Bonus Cooldown**
```
/slregen player <playerName> cooldown daybonus reset
```
Reset the 24000-tick survival timer, allowing the player to claim the daily bonus again immediately.

Example:
```
/slregen player Steve cooldown daybonus reset
```

**Reset Inn Warmup Timer**
```
/slregen player <playerName> cooldown innwarmup reset
```
Reset the Inn warmup timer (useful if a player is stuck in warmup state).

Example:
```
/slregen player Steve cooldown innwarmup reset
```

---

#### Status & Reset

**View All Player Stats**
```
/slregen player <playerName> status
```
Display a complete overview of the player's regeneration stats in a single message.

Example:
```
/slregen player Steve status
```
Output:
```
[SLRegen] === Steve's Status ===
Fatigue: 15.2 / 40.0
Max Capacity: 40.0
Level: 3
Total Fatigue Spent: 250.5
Exhausted: NO
```

**Hard Reset Player Stats**
```
/slregen player <playerName> reset
```
Perform a complete reset: fatigue = 0, maxCap = BASE, level = 0. (Note: totalFatigueSpent is NOT reset.)

Example:
```
/slregen player Steve reset
```
Output: `[SLRegen] HARD RESET Steve: fatigue=0, capacity=BASE (40.0), level=0`

---

### ActionBar Status Display (OP level 2 to view, player-specific)

#### Enable Persistent ActionBar

**Toggle ActionBar On**
```
/slregen bar on
```
Enable the persistent regenerative capacity bar on your action bar. The bar will display every tick while active.

Example:
```
/slregen bar on
```
Output: `[SLRegen] Status bar ENABLED - you will see your regenerative capacity on the action bar.`

#### Disable ActionBar

**Toggle ActionBar Off**
```
/slregen bar off
```
Disable the capacity bar display.

Example:
```
/slregen bar off
```
Output: `[SLRegen] Status bar DISABLED.`

#### Check ActionBar Status

**Check Current State**
```
/slregen bar status
```
View whether the action bar is currently enabled or disabled for you.

Example:
```
/slregen bar status
```
Output: `[SLRegen] Status bar is currently: ENABLED`

---

## Configuration

The mod can be configured via `config/soulslikeregen-common.toml`:

```toml
[general]
base_max_cap = 40.0                    # Starting capacity (in half-hearts)
nexus_drain_rate = 1.0                 # Fatigue units per second in Nexus
nexus_drain_interval_ticks = 20        # Ticks between drain pulses
inn_drain_rate = 0.5                   # Fatigue units per drain pulse in Inn
inn_warmup_ticks = 1200                # Ticks to wait before Inn drain starts (60s)
inn_drain_interval_ticks = 40          # Ticks between Inn drain pulses
day_bonus_reduction = 40.0             # Fatigue drained after 24000 ticks without damage
bed_rest_reduction = 20.0              # Fatigue drained per sleep
campfire_warmup_ticks = 600            # Ticks to stand near campfire before recovery
campfire_reduction = 5.0               # Fatigue drained after campfire warmup
ally_discount_per_player = 0.10        # 10% faster drain per nearby ally in Nexus

[[levels]]
capacity_increase = 20.0               # Capacity increase for this level
fatigue_threshold = 400.0              # Cumulative fatigue to reach this level

[[levels]]
capacity_increase = 20.0
fatigue_threshold = 1400.0

[[levels]]
capacity_increase = 30.0
fatigue_threshold = 3000.0
```

### Level System

The level system is **cumulative**: as you spend fatigue through healing, your `totalFatigueSpent` increases. When it crosses a level threshold, your `maxCap` increases permanently. For example:

- **Level 0** (Base): maxCap = 40.0, requires 0 fatigue spent
- **Level 1**: maxCap = 60.0, requires 400 fatigue spent
- **Level 2**: maxCap = 80.0, requires 1400 fatigue spent (cumulative)
- **Level 3**: maxCap = 110.0, requires 3000 fatigue spent (cumulative)

## Integrations

### FTB Teams (Optional)

If **FTB Teams** is installed:
- Team Nexus zones are team-specific: only team members drain fatigue inside their team's Nexus
- Ally proximity bonus applies: for each nearby team member, fatigue drains 10% faster
- If FTB Teams is not installed, Nexus zones function as global Inns (open to everyone)

### Waystones (Optional)

If **Waystones** is installed:
- Using/activating a Waystone instantly resets your `currentFatigue` to 0
- Useful for long-distance travel without losing all capacity

## How It Works

1. **Natural Healing**: When your food is full and you take damage, natural regen attempts to heal you. Each point of health healed **increases your fatigue** by 1.0.

2. **Exhaustion**: When `currentFatigue >= maxCap`, your **healing is completely blocked**—no health increases, and no food is consumed (UHC-style).

3. **Recovery**:
   - **Team Nexus**: Drain 1 unit/second (20 units every 20 ticks)
   - **Inn**: After 1 minute warmup, drain 0.5 units every 40 ticks (0.5/sec)
   - **Waystone**: Instant full reset to 0
   - **Bed**: Once per in-game day, drain 20 units
   - **Campfire**: After 30 seconds stationary near a lit campfire, drain 5 units (once per day)
   - **Day Survival**: After 24000 ticks (20 min) without taking damage, drain 40 units

4. **Level-Up**: As you accumulate fatigue spending, your `maxCap` increases permanently according to the configured level thresholds.

## Command Examples Cheat Sheet

```bash
# Zone Setup
/slregen setTeamNexus 0 64 0 25 "Team A"
/slregen setInn 500 64 500 30
/slregen listNexus
/slregen listInns

# Player Stat Queries
/slregen player Steve fatigue get
/slregen player Steve capacity get
/slregen player Steve level get
/slregen player Steve status

# Player Stat Modifications
/slregen player Steve fatigue set 0
/slregen player Steve capacity reset
/slregen player Steve level reset
/slregen player Steve reset                    # Hard reset all stats

# ActionBar Toggle
/slregen bar on
/slregen bar status
/slregen bar off
```

## Troubleshooting

**Healing is blocked but I don't see an exhausted message:**
- Your capacity may be exhausted. Use `/slregen player <name> status` to check fatigue levels.

**Nexus isn't working:**
- Verify you're standing inside the zone radius.
- If using FTB Teams, ensure you're in the correct team.
- Check that the Nexus coordinates are correct with `/slregen listNexus`.

**ActionBar isn't showing:**
- Enable it with `/slregen bar on`.
- Verify TEST_MODE is not enabled in config.

**Waystones aren't resetting fatigue:**
- Ensure Waystones mod is installed.
- Check that you're actually using a Waystone (activate it, don't just walk near it).

## License

All Rights Reserved. KMC Soulslike Regen © 2026