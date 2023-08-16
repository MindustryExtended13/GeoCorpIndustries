package gmod.ui;

import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.event.ClickListener;
import arc.scene.event.HandCursorListener;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import gmod.GeoCorp;
import gmod.parts.Part;
import gmod.parts.PartsCategory;
import gmod.util.GeoGroups;
import gmod.world.block.units.SpaceShipConstructor.SpaceShipConstructorBuild;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class PartsEditorDialog extends BaseDialog {
    public SpaceShipConstructorBuild build;
    public PartsCategory partsCategory;
    public PartsEditorElement element;

    public PartsEditorDialog(SpaceShipConstructorBuild build) {
        super("parts");
        this.build = build;
        clearChildren();
        element = new PartsEditorElement(build);
        add(element).grow().row();
        table(menu -> {
            menu.setBackground(Styles.black);
            menu.table(pane -> {
                pane.setBackground(Tex.paneRight);
                pane.table(button -> {
                    button.defaults().size(48);
                    button.button(Icon.copy, Styles.cleari, () -> {
                    }).tooltip("Copy");
                    button.button(Icon.pencil, Styles.cleari, () -> {
                    }).tooltip("Place mod");
                    button.button(Icon.cancel, Styles.cleari, () -> {
                    }).tooltip("Delete mode");
                    button.button(Icon.rotate, Styles.cleari, () -> {
                        if(element.hasCurrent()) {
                            element.current.rotation = (element.current.rotation + 1) % 4;
                        }
                    }).tooltip("Rotate part");
                    button.row();
                    button.button(new TextureRegionDrawable(GeoCorp.asset("mirror-x")), Styles.clearTogglei,
                            () -> {}).tooltip("Mirror X");
                    button.button(new TextureRegionDrawable(GeoCorp.asset("mirror-y")), Styles.clearTogglei,
                            () -> {}).tooltip("Mirror Y");
                }).grow().row();
                pane.button("@close", Icon.left, this::hide).size(150, 40);
            }).pad(6).growY();
            menu.table(selection -> {
                final Table[] el = new Table[1];
                Runnable rebuild = () -> {
                    Table e = el[0];
                    e.clearChildren();
                    if(partsCategory != null) {
                        for(Part part : partsCategory.parts) {
                            Stack stack = new Stack();
                            stack.add(new Table(o -> {
                                o.left();
                                o.add(new Image(GeoCorp.asset(
                                        "default-part-background"
                                ))).size(75).scaling(Scaling.fit);
                            }));
                            for(TextureRegion icon : part.drawer.icons()) {
                                stack.add(new Table(o -> {
                                    o.left();
                                    o.add(new Image(icon)).size(50).padLeft(25/2f).scaling(Scaling.fit);
                                }));
                            }
                            ClickListener listener = new ClickListener();
                            stack.addListener(listener);
                            stack.addListener(new HandCursorListener());
                            stack.clicked(() -> {
                                element.current.part = part;
                            });
                            e.add(stack).padRight(6).size(75).tooltip(t -> {
                                t.setBackground(Tex.button);
                                t.table(stats -> {
                                    stats.defaults().left();
                                    stats.add(part.localizedName()).row();
                                    stats.add(part.description()).row();
                                    stats.add("Size: " + part.width + "x" + part.height);
                                });
                            });
                        }
                    }
                };
                selection.pane(categories -> {
                    categories.left();
                    categories.defaults().size(32);
                    GeoGroups.PARTS_CATEGORIES.each(category -> {
                        categories.button(new TextureRegionDrawable(category.icon), Styles.cleari, () -> {
                            partsCategory = category;
                            rebuild.run();
                        }).tooltip(category.localizedName());
                    });
                }).padLeft(6).growX().row();
                selection.pane(elements -> {
                    elements.left();
                    el[0] = elements;
                }).padLeft(6).grow();
            }).pad(6).grow();
        }).height(176).growX();
    }

    public boolean validPart(Part part) {
        return build.isCreative();
    }
}
