package gmod.entity;

import arc.Events;
import arc.func.Boolf;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Structs;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pools;
import gmod.GeoCorp;
import gmod.parts.PartEntity;
import gmod.parts.PartEntityRegister;
import gmod.type.MultiAmmoType;
import me13.core.units.XeonUnitEntity;
import mindustry.Vars;
import mindustry.ai.types.CommandAI;
import mindustry.ai.types.LogicAI;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.abilities.Ability;
import mindustry.entities.units.BuildPlan;
import mindustry.entities.units.StatusEntry;
import mindustry.entities.units.UnitController;
import mindustry.entities.units.WeaponMount;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.input.InputHandler;
import mindustry.logic.LAccess;
import mindustry.type.AmmoType;
import mindustry.type.Item;
import mindustry.type.StatusEffect;
import mindustry.world.Build;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.storage.CoreBlock;
import org.jetbrains.annotations.NotNull;

public class AbstractUnitEntity extends XeonUnitEntity {
    public Seq<PartEntity> entities = new Seq<>();

    public float buildSpeed, buildRange, range, mineRange, strafePenalty, accel, rotateSpeed,
            buildSpeedMultiplayer, mineSpeed, speed, clipSize, maxRange, aimDst;
    public boolean hasWeapons, hittable, rotateToBuilding, omniMovement, mineFloor, mineWalls;
    public ObjectSet<StatusEffect> immunities = new ObjectSet<>();
    public int ammoCap, mineTier, itemCapacity;
    public AmmoType ammoType;

    {
        resetStats();
    }

    public void updateStats() {
        resetStats();
        entities.each(PartEntity::updateStats);
    }

    public void addAbility(Ability ability) {
        if(ability == null) return;
        Ability[] old = abilities;
        Ability[] n = new Ability[old.length + 1];
        for(int i = 0; i < old.length; i++) {
            n[i] = old[i];
        }
        n[old.length] = ability;
        ability.init(type);
        abilities = n;
    }

    public void addAmmoType(AmmoType type) {
        if(ammoType instanceof MultiAmmoType) {
            ((MultiAmmoType) ammoType).types.add(type);
        } else {
            ammoType = new MultiAmmoType(ammoType, type);
        }
    }

    public void resetAmmoType() {
        ammoType = new AmmoType() {
            @Override
            public String icon() {
                return "";
            }

            @Override
            public Color color() {
                return Color.white;
            }

            @Override
            public Color barColor() {
                return Color.white;
            }

            @Override
            public void resupply(Unit unit) {
            }
        };
    }

    public void resetStats() {
        type.
        buildSpeed = 0f;
        buildRange = 80f;
        range = 80f;
        speed = 0;
        accel = 0.5f;
        armor = 0;
        drag = 0.3f;
        abilities = new Ability[0];
        maxRange = -1f;
        rotateSpeed = 1;
        strafePenalty = 0.5f;
        mineFloor = true;
        mineWalls = false;
        immunities.clear();
        hasWeapons = false;
        hittable = true;
        omniMovement = false;
        rotateToBuilding = true;
        buildSpeedMultiplayer = 0f;
        mineSpeed = 0f;
        mineTier = 0;
        ammoCap = 0;
        mineRange = 80f;
        itemCapacity = 0;
        clipSize = -1f;
        maxHealth = 16;
        resetAmmoType();
        aimDst = -1f;
        hitSize = Math.max(unitHeight(), unitWidth());
    }

    public float unitWidth() {
        PartEntity max = entities.max(PartEntity::getX);
        PartEntity min = entities.min(PartEntity::getX);
        if(max == null || min == null) return 0;
        float mx = max.getX(), mix = min.getX();
        return Math.abs(Math.max(mx, mix) - Math.min(mx, mix)) + max.width()/2 + min.width()/2;
    }

    public float unitHeight() {
        PartEntity max = entities.max(PartEntity::getY);
        PartEntity min = entities.min(PartEntity::getY);
        if(max == null || min == null) return 0;
        float may = max.getY(), miy = min.getY();
        return Math.abs(Math.max(may, miy) - Math.min(may, miy)) + max.height()/2 + min.height()/2;
    }

    public void addPart(PartEntity entity) {
        GeoCorp.requireNonNull(entity);
        entity.init(this);
        entities.add(entity);
    }

    public Vec2 relative(@NotNull Position p) {
        float x = p.getX(), y = p.getY();
        return new Vec2(
                this.x + Angles.trnsx(rotation - 90, x, y),
                this.y + Angles.trnsy(rotation - 90, x, y)
        );
    }

    public void aim(float x, float y) {
        Tmp.v1.set(x, y).sub(this.x, this.y);
        if (Tmp.v1.len() < this.aimDst) {
            Tmp.v1.setLength(this.aimDst);
        }

        x = Tmp.v1.x + this.x;
        y = Tmp.v1.y + this.y;
        WeaponMount[] var3 = this.mounts;

        for (WeaponMount mount : var3) {
            if (mount.weapon.controllable) {
                mount.aimX = x;
                mount.aimY = y;
            }
        }

        this.aimX = x;
        this.aimY = y;
    }

    @Override
    public void approach(Vec2 vector) {
        this.vel.approachDelta(vector, this.accel * this.speed());
    }

    @Override
    public Item getMineResult(Tile tile) {
        if (tile == null) {
            return null;
        } else {
            Item result;
            if (mineFloor && tile.block() == Blocks.air) {
                result = tile.drop();
            } else {
                if (!mineWalls) {
                    return null;
                }

                result = tile.wallDrop();
            }

            return this.canMine(result) ? result : null;
        }
    }

    @Override
    public int itemCapacity() {
        return itemCapacity;
    }

    @Override
    public int maxAccepted(Item item) {
        return this.stack.item != item && this.stack.amount > 0 ? 0 : this.itemCapacity() - this.stack.amount;
    }

    @Override
    public float speed() {
        float strafePenalty = !this.isGrounded() && this.isPlayer() ?
                Mathf.lerp(1.0F, this.strafePenalty,
                        Angles.angleDist(this.vel().angle(), this.rotation) / 180.0F) : 1.0F;
        return this.speed * strafePenalty * this.floorSpeedMultiplier();
    }

    @Override
    public float range() {
        return this.maxRange;
    }

    @Override
    public float prefRotation() {
        if (this.activelyBuilding() && rotateToBuilding) {
            return this.angleTo(this.buildPlan());
        } else if (this.mineTile != null) {
            return this.angleTo(this.mineTile);
        } else {
            return this.moving() && omniMovement ? this.vel().angle() : this.rotation;
        }
    }

    @Override
    public float clipSize() {
        if (this.isBuilding()) {
            return Vars.state.rules.infiniteResources ? Float.MAX_VALUE : Math.max(clipSize,
                    Math.max(unitHeight(), unitWidth()) + buildRange + 32.0F);
        } else {
            return this.mining() ? clipSize + mineRange : clipSize;
        }
    }

    @Override
    public void lookAt(float angle) {
        this.rotation = Angles.moveToward(this.rotation, angle, rotateSpeed * Time.delta * this.speedMultiplier());
    }

    @Override
    public boolean validMine(Tile tile, boolean checkDst) {
        if (tile == null) {
            return false;
        } else if (checkDst && !this.within(tile.worldx(), tile.worldy(), mineRange)) {
            return false;
        } else {
            return this.getMineResult(tile) != null;
        }
    }

    @Override
    public double sense(LAccess sensor) {
        double var10000;
        switch (sensor) {
            case totalItems -> var10000 = this.stack().amount;
            case itemCapacity -> var10000 = itemCapacity;
            case rotation -> var10000 = this.rotation;
            case health -> var10000 = this.health;
            case maxHealth -> var10000 = this.maxHealth;
            case ammo -> var10000 = !Vars.state.rules.unitAmmo ? (double) ammoCap : (double) this.ammo;
            case ammoCapacity -> var10000 = ammoCap;
            case x -> var10000 = World.conv(this.x);
            case y -> var10000 = World.conv(this.y);
            case dead -> var10000 = !this.dead && this.isAdded() ? 0.0 : 1.0;
            case team -> var10000 = this.team.id;
            case shooting -> var10000 = this.isShooting() ? 1.0 : 0.0;
            case boosting -> var10000 = 0.0;
            case range -> var10000 = this.range() / 8.0F;
            case shootX -> var10000 = World.conv(this.aimX());
            case shootY -> var10000 = World.conv(this.aimY());
            case mining -> var10000 = this.mining() ? 1.0 : 0.0;
            case mineX -> var10000 = this.mining() ? (double) this.mineTile.x : -1.0;
            case mineY -> var10000 = this.mining() ? (double) this.mineTile.y : -1.0;
            case flag -> var10000 = this.flag;
            case speed -> var10000 = speed * 60.0F / 8.0F;
            case controlled -> {
                byte var5;
                if (!this.isValid()) {
                    var5 = 0;
                } else if (this.controller instanceof LogicAI) {
                    var5 = 1;
                } else if (this.controller instanceof Player) {
                    var5 = 2;
                } else {
                    label90:
                    {
                        UnitController var3 = this.controller;
                        if (var3 instanceof CommandAI command) {
                            if (command.hasCommand()) {
                                var5 = 3;
                                break label90;
                            }
                        }

                        var5 = 0;
                    }
                }
                var10000 = var5;
            }
            case payloadCount -> {
                if (this instanceof Payloadc pay) {
                    var10000 = pay.payloads().size;
                } else {
                    var10000 = 0.0;
                }
            }
            case size -> var10000 = this.hitSize / 8.0F;
            case color -> var10000 = Color.toDoubleBits(this.team.color.r,
                    this.team.color.g, this.team.color.b, 1.0F);
            default -> var10000 = Double.NaN;
        }

        return var10000;
    }

    @Override
    public float ammof() {
        return this.ammo / (float) ammoCap;
    }

    @Override
    public boolean targetable(Team targeter) {
        return hasWeapons;
    }

    @Override
    public void setProp(UnlockableContent content, double value) {
        if (content instanceof Item) {
            this.stack.item = (Item) content;
            this.stack.amount = Mathf.clamp((int)value, 0, itemCapacity);
        }
    }

    @Override
    public void rotateMove(Vec2 vec) {
        this.moveAt(Tmp.v2.trns(this.rotation, vec.len()));
        if (!vec.isZero()) {
            this.rotation = Angles.moveToward(this.rotation, vec.angle(), rotateSpeed * Time.delta);
        }
    }

    @Override
    public void movePref(Vec2 movement) {
        if (this.omniMovement) {
            this.moveAt(movement);
        } else {
            this.rotateMove(movement);
        }
    }

    @Override
    public void moveAt(Vec2 vector) {
        this.moveAt(vector, accel);
    }

    @Override
    public boolean shouldSkip(BuildPlan plan, Building core) {
        if (!Vars.state.rules.infiniteResources &&
                !this.team.rules().infiniteResources &&
                !plan.breaking && core != null && !plan.isRotation(this.team) &&
                (!this.isBuilding() || this.within(this.plans.last(), buildRange))) {
            return plan.stuck && !core.items.has(plan.block.requirements) ||
                    Structs.contains(plan.block.requirements, (i) -> {
                return !core.items.has(i.item, Math.min(i.amount, 15)) &&
                        Mathf.round((float)i.amount * Vars.state.rules.buildCostMultiplier) > 0;
            }) && !plan.initialized;
        } else {
            return false;
        }
    }

    @Override
    public boolean isPathImpassable(int tileX, int tileY) {
        return false;
    }

    @Override
    public boolean isImmune(StatusEffect effect) {
        return immunities.contains(effect);
    }

    @Override
    public boolean inRange(Position other) {
        return this.within(other, this.range);
    }

    @Override
    public boolean activelyBuilding() {
        if (this.isBuilding()) {
            BuildPlan plan = this.buildPlan();
            if (!Vars.state.isEditor() && plan != null && !this.within(plan,
                    Vars.state.rules.infiniteResources ? Float.MAX_VALUE : buildRange)) {
                return false;
            }
        }

        return this.isBuilding() && this.updateBuilding;
    }

    @Override
    public boolean hittable() {
        return hittable;
    }

    @Override
    public boolean isEnemy() {
        return hasWeapons;
    }

    @Override
    public boolean hasWeapons() {
        return hasWeapons;
    }

    @Override
    public boolean canShoot() {
        return !this.disarmed;
    }

    @Override
    public boolean displayable() {
        return true;
    }

    @Override
    public boolean canMine(Item item) {
        if (item == null) {
            return false;
        } else {
            return mineTier >= item.hardness;
        }
    }

    @Override
    public boolean canDrown() {
        return this.isGrounded() && !this.hovering;
    }

    @Override
    public boolean canMine() {
        return mineSpeed > 0.0F && mineTier >= 0;
    }

    @Override
    public boolean canBuild() {
        return buildSpeed > 0.0F && buildSpeedMultiplayer > 0.0F;
    }

    @Override
    public void drawBuildingBeam(float px, float py) {
        boolean active = this.activelyBuilding();
        if (active || this.lastActive != null) {
            Draw.z(115.0F);
            BuildPlan plan = active ? this.buildPlan() : this.lastActive;
            Tile tile = Vars.world.tile(plan.x, plan.y);
            if (tile != null && this.within(plan,
                    Vars.state.rules.infiniteResources ? Float.MAX_VALUE : buildRange)) {
                int size = plan.breaking ? (active ? tile.block().size : this.lastSize) : plan.block.size;
                float tx = plan.drawx();
                float ty = plan.drawy();
                Lines.stroke(1.0F, plan.breaking ? Pal.remove : Pal.accent);
                Draw.z(122.0F);
                Draw.alpha(this.buildAlpha);
                if (!active && !(tile.build instanceof ConstructBlock.ConstructBuild)) {
                    Fill.square(plan.drawx(), plan.drawy(), (float)(size * 8) / 2.0F);
                }

                Drawf.buildBeam(px, py, tx, ty, (float)(8 * size) / 2.0F);
                Fill.square(px, py, 1.8F + Mathf.absin(Time.time, 2.2F, 1.1F), this.rotation + 45.0F);
                Draw.reset();
                Draw.z(115.0F);
            }
        }
    }

    @Override
    public void drawBuilding() {
    }

    @Override
    public void update() {
        updateStats();
        float offset;
        float range;
        if (!Vars.net.client() || this.isLocal()) {
            offset = this.x;
            range = this.y;
            this.move(this.vel.x * Time.delta, this.vel.y * Time.delta);
            if (Mathf.equal(offset, this.x)) {
                this.vel.x = 0.0F;
            }

            if (Mathf.equal(range, this.y)) {
                this.vel.y = 0.0F;
            }

            this.vel.scl(Math.max(1.0F - this.drag * Time.delta, 0.0F));
        }

        float cx;
        float cy;
        offset = 0.0F;
        range = 0.0F;
        cx = (float)Vars.world.unitHeight();
        cy = (float)Vars.world.unitWidth();
        if (Vars.state.rules.limitMapArea && !this.team.isAI()) {
            offset = (float)(Vars.state.rules.limitY * 8);
            range = (float)(Vars.state.rules.limitX * 8);
            cx = (float)(Vars.state.rules.limitHeight * 8) + offset;
            cy = (float)(Vars.state.rules.limitWidth * 8) + range;
        }

        if (!Vars.net.client() || this.isLocal()) {
            float dx = 0.0F;
            float dy = 0.0F;
            if (this.x < range) {
                dx += -(this.x - range) / 30.0F;
            }

            if (this.y < offset) {
                dy += -(this.y - offset) / 30.0F;
            }

            if (this.x > cy) {
                dx -= (this.x - cy) / 30.0F;
            }

            if (this.y > cx) {
                dy -= (this.y - cx) / 30.0F;
            }

            this.velAddNet(dx * Time.delta, dy * Time.delta);
        }

        if (this.isGrounded()) {
            this.x = Mathf.clamp(this.x, range, cy - 8.0F);
            this.y = Mathf.clamp(this.y, offset, cx - 8.0F);
        }

        if (this.x < -250.0F + range || this.y < -250.0F + offset || this.x >= cy + 250.0F ||
                this.y >= cx + 250.0F) {
            this.kill();
        }

        this.updateBuildLogic();
        Floor floor = this.floorOn();
        if (this.isFlying() != this.wasFlying) {
            if (this.wasFlying && this.tileOn() != null) {
                Fx.unitLand.at(this.x, this.y, this.floorOn().isLiquid ? 1.0F : 0.5F,
                        this.tileOn().floor().mapColor);
            }

            this.wasFlying = this.isFlying();
        }

        if (!this.hovering && this.isGrounded() && (this.splashTimer +=
                Mathf.dst(this.deltaX(), this.deltaY())) >= 7.0F + this.hitSize() / 8.0F) {
            floor.walkEffect.at(this.x, this.y, this.hitSize() / 8.0F, floor.mapColor);
            this.splashTimer = 0.0F;
            if (this.emitWalkSound()) {
                floor.walkSound.at(this.x, this.y, Mathf.random(floor.
                        walkSoundPitchMin, floor.walkSoundPitchMax), floor.walkSoundVolume);
            }
        }

        this.updateDrowning();
        this.hitTime -= Time.delta / 9.0F;
        this.stack.amount = Mathf.clamp(this.stack.amount, 0, this.itemCapacity());
        this.itemTime = Mathf.lerpDelta(this.itemTime, (float)Mathf.num(this.hasItem()), 0.05F);
        int accepted;
        if (this.mineTile != null) {
            Building core = this.closestCore();
            Item item = this.getMineResult(this.mineTile);
            if (core != null && item != null && !this.acceptsItem(item) &&
                    this.within(core, 220.0F) && !this.offloadImmediately()) {
                accepted = core.acceptStack(this.item(), this.stack().amount, this);
                if (accepted > 0) {
                    Call.transferItemTo(this, this.item(), accepted, this.mineTile.worldx() +
                            Mathf.range(4.0F), this.mineTile.worldy() + Mathf.range(4.0F), core);
                    this.clearItem();
                }
            }

            if ((!Vars.net.client() || this.isLocal()) && !this.validMine(this.mineTile)) {
                this.mineTile = null;
                this.mineTimer = 0.0F;
            } else if (this.mining() && item != null) {
                this.mineTimer += Time.delta * mineSpeed;
                if (Mathf.chance(0.06 * (double) Time.delta)) {
                    Fx.pulverizeSmall.at(this.mineTile.worldx() +
                            Mathf.range(4.0F), this.mineTile.worldy() +
                            Mathf.range(4.0F), 0.0F, item.color);
                }

                if (this.mineTimer >= 50.0F + ((float) item.hardness * 15.0F)) {
                    this.mineTimer = 0.0F;
                    if (Vars.state.rules.sector != null && this.team() == Vars.state.rules.defaultTeam) {
                        Vars.state.rules.sector.info.handleProduction(item, 1);
                    }

                    if (core != null && this.within(core, 220.0F) &&
                            core.acceptStack(item, 1, this) == 1 && this.offloadImmediately()) {
                        if (this.item() == item && !Vars.net.client()) {
                            this.addItem(item);
                        }

                        Call.transferItemTo(this, item, 1,
                                this.mineTile.worldx() + Mathf.range(4.0F),
                                this.mineTile.worldy() + Mathf.range(4.0F), core);
                    } else if (this.acceptsItem(item)) {
                        InputHandler.transferItemToUnit(item,
                                this.mineTile.worldx() + Mathf.range(4.0F),
                                this.mineTile.worldy() + Mathf.range(4.0F), this);
                    } else {
                        this.mineTile = null;
                        this.mineTimer = 0.0F;
                    }
                }

                if (!Vars.headless) {
                    Vars.control.sound.loop(this.type.mineSound, this, this.type.mineSoundVolume);
                }
            }
        }

        this.shieldAlpha -= Time.delta / 15.0F;
        if (this.shieldAlpha < 0.0F) {
            this.shieldAlpha = 0.0F;
        }

        floor = this.floorOn();
        if (this.isGrounded() && !this.type.hovering) {
            this.apply(floor.status, floor.statusDuration);
        }

        this.applied.clear();
        this.speedMultiplier = this.damageMultiplier = this.healthMultiplier =
                this.reloadMultiplier = this.buildSpeedMultiplier = this.dragMultiplier = 1.0F;
        this.disarmed = false;
        int index;
        if (!this.statuses.isEmpty()) {
            index = 0;

            label318:
            while(true) {
                while(true) {
                    if (index >= this.statuses.size) {
                        break label318;
                    }

                    StatusEntry entry = this.statuses.get(index++);
                    entry.time = Math.max(entry.time - Time.delta, 0.0F);
                    if (entry.effect != null && (!(entry.time <= 0.0F) || entry.effect.permanent)) {
                        this.applied.set(entry.effect.id);
                        this.speedMultiplier *= entry.effect.speedMultiplier;
                        this.healthMultiplier *= entry.effect.healthMultiplier;
                        this.damageMultiplier *= entry.effect.damageMultiplier;
                        this.reloadMultiplier *= entry.effect.reloadMultiplier;
                        this.buildSpeedMultiplier *= entry.effect.buildSpeedMultiplier;
                        this.dragMultiplier *= entry.effect.dragMultiplier;
                        this.disarmed |= entry.effect.disarm;
                        entry.effect.update(this, entry.time);
                    } else {
                        Pools.free(entry);
                        --index;
                        this.statuses.remove(index);
                    }
                }
            }
        }

        if (Vars.net.client() && !this.isLocal() || this.isRemote()) {
            this.interpolate();
        }

        if (this.wasHealed && this.healTime <= -1.0F) {
            this.healTime = 1.0F;
        }

        this.healTime -= Time.delta / 20.0F;
        this.wasHealed = false;
        if (this.team.isOnlyAI() && Vars.state.isCampaign() && Vars.state.getSector().isCaptured()) {
            this.kill();
        }

        if (!Vars.headless && this.type.loopSound != Sounds.none) {
            Vars.control.sound.loop(this.type.loopSound, this, this.type.loopSoundVolume);
        }

        if (Vars.state.rules.unitAmmo && this.ammo < (float)ammoCap - 1.0E-4F) {
            this.resupplyTime += Time.delta;
            if (this.resupplyTime > 10.0F) {
                ammoType.resupply(this);
                this.resupplyTime = 0.0F;
            }
        }

        Ability[] var10 = this.abilities;
        index = var10.length;

        for(accepted = 0; accepted < index; ++accepted) {
            Ability a = var10[accepted];
            a.update(this);
        }

        this.drag = (this.isGrounded() ? this.floorOn().dragMultiplier : 1.0F) *
                this.dragMultiplier * Vars.state.rules.dragMultiplier;
        if (this.team != Vars.state.rules.waveTeam && Vars.state.hasSpawns() &&
                (!Vars.net.client() || this.isLocal()) && this.hittable()) {
            offset = Vars.state.rules.dropZoneRadius + this.hitSize / 2.0F + 1.0F;
            for (Tile spawn : Vars.spawner.getSpawns()) {
                if (this.within(spawn.worldx(), spawn.worldy(), offset)) {
                    this.velAddNet(Tmp.v1.set(this).sub(spawn.worldx(),
                            spawn.worldy()).setLength(1.1F - this.dst(spawn) / offset)
                            .scl(0.45F * Time.delta));
                }
            }
        }

        if (this.dead || this.health <= 0.0F) {
            this.drag = 0.01F;
            if (Mathf.chanceDelta(0.1)) {
                Tmp.v1.rnd(Mathf.range(this.hitSize));
                this.type.fallEffect.at(this.x + Tmp.v1.x, this.y + Tmp.v1.y);
            }

            if (Mathf.chanceDelta(0.2)) {
                offset = this.type.engineOffset / 2.0F + this.type.engineOffset / 2.0F * this.elevation;
                range = Mathf.range(this.type.engineSize);
                this.type.fallEngineEffect.at(this.x + Angles.trnsx(this.rotation + 180.0F, offset) +
                        Mathf.range(range), this.y + Angles.trnsy(this.rotation + 180.0F, offset) +
                        Mathf.range(range), Mathf.random());
            }

            this.elevation -= this.type.fallSpeed * Time.delta;
            if (this.isGrounded() || this.health <= -this.maxHealth) {
                Call.unitDestroy(this.id);
            }
        }

        Tile tile = this.tileOn();
        Floor floor2 = this.floorOn();
        if (tile != null && this.isGrounded() && !this.type.hovering) {
            if (tile.build != null) {
                tile.build.unitOn(this);
            }

            if (floor2.damageTaken > 0.0F) {
                this.damageContinuous(floor2.damageTaken);
            }
        }

        if (tile != null && !this.canPassOn()) {
            if (this.type.canBoost) {
                this.elevation = 1.0F;
            } else if (!Vars.net.client()) {
                this.kill();
            }
        }

        if (!Vars.net.client() && !this.dead) {
            this.controller.updateUnit();
        }

        if (!this.controller.isValidController()) {
            this.resetController();
        }

        if (this.spawnedByCore && !this.isPlayer() && !this.dead) {
            Call.unitDespawn(this);
        }

        WeaponMount[] var16 = this.mounts;
        index = var16.length;

        for(accepted = 0; accepted < index; ++accepted) {
            WeaponMount mount = var16[accepted];
            mount.weapon.update(this, mount);
        }
        updateStats();
        entities.each(PartEntity::update);
    }

    @Override
    public void updateBuildLogic() {
        if (!(buildSpeed <= 0.0F)) {
            if (!Vars.headless) {
                if (this.lastActive != null && this.buildAlpha <= 0.01F) {
                    this.lastActive = null;
                }

                this.buildAlpha = Mathf.lerpDelta(this.buildAlpha,
                        this.activelyBuilding() ? 1.0F : 0.0F, 0.15F);
            }

            if (this.updateBuilding && this.canBuild()) {
                float finalPlaceDst = Vars.state.rules.infiniteResources ?
                        Float.MAX_VALUE : buildRange;
                boolean infinite = Vars.state.rules.infiniteResources ||
                        this.team().rules().infiniteResources;
                this.buildCounter += Time.delta;
                if (Float.isNaN(this.buildCounter) || Float.isInfinite(this.buildCounter)) {
                    this.buildCounter = 0.0F;
                }

                this.buildCounter = Math.min(this.buildCounter, 10.0F);
                int maxPerFrame = 10;
                int count = 0;

                while (this.buildCounter >= 1.0F && count++ < maxPerFrame) {
                    --this.buildCounter;
                    this.validatePlans();
                    CoreBlock.CoreBuild core = this.core();
                    if (this.buildPlan() == null) {
                        return;
                    }

                    if (this.plans.size > 1) {
                        int total = 0;

                        BuildPlan plan;
                        for (int size = this.plans.size; (!this.within((plan =
                                this.buildPlan()).tile(), finalPlaceDst) ||
                                this.shouldSkip(plan, core)) && total < size; ++total) {
                            this.plans.removeFirst();
                            this.plans.addLast(plan);
                        }
                    }

                    BuildPlan current = this.buildPlan();
                    Tile tile = current.tile();
                    this.lastActive = current;
                    this.buildAlpha = 1.0F;
                    if (current.breaking) {
                        this.lastSize = tile.block().size;
                    }

                    if (this.within(tile, finalPlaceDst)) {
                        if (!Vars.headless) {
                            Vars.control.sound.loop(Sounds.build, tile, 0.15F);
                        }

                        Building var9 = tile.build;
                        ConstructBlock.ConstructBuild entity;
                        if (var9 instanceof ConstructBlock.ConstructBuild) {
                            entity = (ConstructBlock.ConstructBuild) var9;
                            if (tile.team() != this.team && tile.team() != Team.derelict ||
                                    !current.breaking && (entity.current != current.block ||
                                            entity.tile != current.tile())) {
                                this.plans.removeFirst();
                                continue;
                            }
                        } else if (!current.initialized && !current.breaking &&
                                Build.validPlace(current.block, this.team, current.x, current.y,
                                        current.rotation)) {
                            boolean hasAll = infinite || current.isRotation(this.team) ||
                                    !Structs.contains(current.block.requirements, (i) -> {
                                        return core != null && !core.items.has(i.item,
                                                Math.min(Mathf.round((float) i.amount *
                                                        Vars.state.rules.buildCostMultiplier), 1));
                                    });
                            if (hasAll) {
                                Call.beginPlace(this, current.block, this.team,
                                        current.x, current.y, current.rotation);
                            } else {
                                current.stuck = true;
                            }
                        } else {
                            if (current.initialized || !current.breaking ||
                                    !Build.validBreak(this.team, current.x, current.y)) {
                                this.plans.removeFirst();
                                continue;
                            }

                            Call.beginBreak(this, this.team, current.x, current.y);
                        }

                        if (tile.build instanceof ConstructBlock.ConstructBuild && !current.initialized) {
                            Events.fire(new EventType.BuildSelectEvent(tile,
                                    this.team, this, current.breaking));
                            current.initialized = true;
                        }

                        if (core != null || infinite) {
                            var9 = tile.build;
                            if (var9 instanceof ConstructBlock.ConstructBuild) {
                                entity = (ConstructBlock.ConstructBuild) var9;
                                float bs = 1.0F / entity.buildCost * buildSpeed *
                                        this.buildSpeedMultiplier * Vars.state.rules.buildSpeed(this.team);
                                if (current.breaking) {
                                    entity.deconstruct(this, core, bs);
                                } else {
                                    entity.construct(this, core, bs, current.config);
                                }

                                current.stuck = Mathf.equal(current.progress, entity.progress);
                                current.progress = entity.progress;
                            }
                        }
                    }
                }

            } else {
                this.validatePlans();
            }
        }
    }

    @Override
    public void drawBuildPlans() {
        Boolf<BuildPlan> skip = (planx) -> {
            return planx.progress > 0.01F || this.buildPlan() == planx && planx.initialized && (
                    this.within((float)(planx.x * 8), (float)(planx.y * 8), buildRange) ||
                            Vars.state.isEditor());
        };

        for(int i = 0; i < 2; ++i) {
            for (BuildPlan plan : this.plans) {
                if (!skip.get(plan)) {
                    if (i == 0) {
                        this.drawPlan(plan, 1.0F);
                    } else {
                        this.drawPlanTop(plan, 1.0F);
                    }
                }
            }
        }

        Draw.reset();
    }

    @Override
    public void draw() {
        entities.each(PartEntity::draw);
        entities.each(PartEntity::drawLight);
    }

    @Override
    public void remove() {
        super.remove();
        entities.each(PartEntity::remove);
    }

    @Override
    public void write(Writes write) {
        super.write(write);
        write.i(entities.size);
        entities.each(entity -> {
            write.i(entity.classID());
        });
        entities.each(entity -> {
            entity.write(write);
        });
    }

    @Override
    public void read(Reads read) {
        super.read(read);
        int s = read.i();
        for(int i = 0; i < s; i++) {
            addPart(PartEntityRegister.get(read.i()));
        }
        entities.each(entity -> {
            entity.read(read);
        });
    }
}