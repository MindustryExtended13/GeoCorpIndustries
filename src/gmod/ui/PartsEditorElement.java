package gmod.ui;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.ScissorStack;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.Image;
import arc.struct.Seq;
import gmod.GeoCorp;
import gmod.parts.Part;
import gmod.parts.PartBuildPlan;
import gmod.parts.PartEntity;
import gmod.parts.PartsConstructBuilder;
import gmod.world.block.units.SpaceShipConstructor.SpaceShipConstructorBuild;
import mindustry.graphics.Pal;
import org.jetbrains.annotations.NotNull;

import static arc.Core.*;
import static gmod.graphics.PartsGraphics.*;

public class PartsEditorElement extends Image {
    private final Rect scissorBounds = new Rect();
    private final Rect widgetAreaBounds = new Rect();
    public SpaceShipConstructorBuild build;
    public TextureRegion tileBSprite;
    public TextureRegion tileASprite;
    public TextureRegion solid;
    public TextureRegion space;
    public PartBuildPlan current = new PartBuildPlan(null, 0, 0);
    public PartEntity selected = null;
    public boolean deletion = false;
    public float scale = 5;
    public float canvasX = 0;
    public float canvasY = 0;
    public float mouseX = 0;
    public float mouseY = 0;

    public float anchorx;
    public float anchory;
    public float prevcanvasx;
    public float prevcanvasy;

    public static float minScale = 1f;
    public static float maxScale = 10f;

    public void updateSelected() {
        selected = null;
        Point2 out = uiToGrid(mouseX, mouseY);
        for(PartEntity entity : build.builder.entities) {
            int w = entity.is2() ? entity.part.height : entity.part.width;
            int h = entity.is2() ? entity.part.width : entity.part.height;
            if(((out.x + 1) > entity.x && (out.x + 1) <= (entity.x + w)) ||
                    (out.x >= entity.x && out.x < (entity.x + w))) {
                if (((out.y + 1) > entity.y && (out.y + 1) <= (entity.y + h)) ||
                        (out.y >= entity.y && out.y < (entity.y + h))) {
                    selected = entity;
                }
            }
        }
    }

    public void scale(float amount) {
        scale = Mathf.clamp(amount, minScale, maxScale);
    }

    public boolean canPlace(@NotNull PartBuildPlan plan) {
        return plan.part.canPlace(plan, this);
    }

    public boolean hasCurrent() {
        return current != null && current.part != null;
    }

    public void handlePlace(Runnable mover) {
        if(!deletion) {
            if(!hasCurrent()) {
                mover.run();
            } else if(canPlace(current)) {
                build.builder.set(current.part, current.x, current.y,
                        current.rotation, current.mirror);
            }
        } else {
            if(selected != null) {
                Seq<PartEntity> entities = new Seq<>();
                for(PartEntity ent : build.builder.entities) {
                    if(ent == selected) {
                        continue;
                    }

                    entities.add(ent);
                }
                build.builder.entities.clear();
                build.builder.entities.add(entities);
            }
        }
    }

    public PartsEditorElement(SpaceShipConstructorBuild build) {
        super();
        this.build = build;
        tileASprite = GeoCorp.asset("tile-a");
        tileBSprite = GeoCorp.asset("tile-b");
        space = GeoCorp.asset("editor-background");
        solid = getRegion();
        addListener(new InputListener() {
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                scale(scale + amountY);
                mouseX = x;
                mouseY = y;
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                mouseX = x;
                mouseY = y;
                handlePlace(() -> {
                    canvasX = (x - anchorx) + prevcanvasx;
                    canvasY = (y - anchory) + prevcanvasy;
                });
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                anchorx = x;
                anchory = y;
                mouseX = x;
                mouseY = y;
                prevcanvasx = canvasX;
                prevcanvasy = canvasY;
                handlePlace(() -> {});
                return true;
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                scene.setScrollFocus(PartsEditorElement.this); ///AAAAAA
                mouseX = x;
                mouseY = y;
                return true;
            }
        });
    }

    @Override
    public void draw() {
        super.draw();
        PartsConstructBuilder b2 = build.builder;
        widgetAreaBounds.set(x,y,width,height);
        scene.calculateScissors(widgetAreaBounds, scissorBounds);
        if(!ScissorStack.push(scissorBounds)) {
            return;
        }
        set(EDITOR_TRANSFORM_X, x + imageX);
        set(EDITOR_TRANSFORM_Y, y + imageY);
        set(EDITOR_WIDTH, imageWidth);
        set(EDITOR_HEIGHT, imageHeight);
        set(EDITOR_SCALE, 1);
        set(EDITOR_OFFSET_X, 0);
        set(EDITOR_OFFSET_Y, 0);
        texture(space, imageWidth/2, imageHeight/2, imageWidth, imageHeight);
        set(EDITOR_SCALE, scale);
        set(EDITOR_OFFSET_X, canvasX);
        set(EDITOR_OFFSET_Y, canvasY);

        float s = Part.PART_TILESIZE;
        boolean b = false;
        float hw = b2.w * s * 0.5f;
        float hh = b2.h * s * 0.5f;
        for(float x = -hw; x < hw; x += s) {
            for(float y = -hh; y < hh; y += s) {
                texture(b ? tileBSprite : tileASprite, x, y, s, s);
                b = !b;
            }
            b = !b;
        }

        Draw.color(Color.red);
        texture(solid, -2, 2, b2.w * s, 1);
        texture(solid, 2, -2, 1, b2.h * s);
        Draw.color(Color.white);
        build.builder.entities.each(PartEntity::draw);
        Point2 out = uiToGrid(mouseX, mouseY);

        if(hasCurrent() && !deletion) {
            Draw.color(canPlace(current) ? Pal.accent : Color.red);
            Draw.alpha(0.7f);
            current.x = out.x;
            current.y = out.y;
            current.part.drawPlan(current);
        }

        if(deletion) {
            updateSelected();
            PartEntity entity = selected;
            Draw.color(Color.red);
            if(entity != null) {
                texture(solid, entity.getX() - 2, entity.getY() - 2,
                        entity.width(), entity.height(), entity.drawRot());
            } else {
                Vec2 v = gridToUI(out.x, out.y);
                texture(solid, v.x, v.y, s, s);
            }
        }

        Draw.reset();
        ScissorStack.pop();
        reset();
    }
}
