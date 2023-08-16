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
import gmod.GeoCorp;
import gmod.parts.Part;
import gmod.parts.PartBuildPlan;
import gmod.parts.PartEntity;
import gmod.parts.PartsConstructBuilder;
import gmod.world.block.units.SpaceShipConstructor.SpaceShipConstructorBuild;

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

    public void scale(float amount) {
        scale = Mathf.clamp(amount, minScale, maxScale);
    }

    public boolean hasCurrent() {
        return current != null && current.part != null;
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
                if(!hasCurrent()) {
                    canvasX = (x - anchorx) + prevcanvasx;
                    canvasY = (y - anchory) + prevcanvasy;
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                anchorx = x;
                anchory = y;
                mouseX = x;
                mouseY = y;
                prevcanvasx = canvasX;
                prevcanvasy = canvasY;
                if(hasCurrent()) {
                    GeoCorp.LOGGER.info("x: {}, y: {}", current.x, current.y);
                    build.builder.set(current.part, current.x, current.y, current.rotation, current.mirror);
                }
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
        if(!ScissorStack.push(scissorBounds)){
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
        for(float x = -hw; x < hw * 2; x += s) {
            for(float y = -hh; y < hh * 2; y += s) {
                texture(b ? tileBSprite : tileASprite, x, y, s, s);
                b = !b;
            }
            b = !b;
        }

        Draw.color(Color.red);
        texture(solid, 2, 2, b2.w * s, 2);
        texture(solid, 2, 2, 2, b2.h * s);
        Draw.color(Color.white);

        build.builder.entities.each(PartEntity::draw);

        if(hasCurrent()) {
            Draw.alpha(0.5f);
            Point2 out = uiToGrid(mouseX, mouseY);
            current.x = out.x;
            current.y = out.y;
            current.part.drawPlan(current);
        }

        Draw.reset();
        ScissorStack.pop();
        reset();
    }
}
