package gmod.ui;

import arc.graphics.g2d.TextureRegion;
import arc.scene.event.ClickListener;
import arc.scene.event.HandCursorListener;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import gmod.parts.Part;
import gmod.parts.PartsCategory;
import gmod.util.GeoGroups;
import gmod.world.block.units.SpaceShipConstructor.SpaceShipConstructorBuild;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static gmod.GeoCorp.*;
import static gmod.util.EnumMirror.*;

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
            Table[] tableA = new Table[1];
            Runnable rebuild2 = () -> {
                Table table = tableA[0];
                table.clearChildren();
                if(element.painting) {
                    table.table(colors -> {
                        colors.defaults().size(150, 40).padTop(3);
                        colors.slider(0, 255, 1, element.r * 255, (ignored) -> {}).update((s) -> {
                            element.r = s.getValue() / 255f;
                        }).padTop(0).row();
                        colors.slider(0, 255, 1, element.g * 255, (ignored) -> {}).update((s) -> {
                            element.g = s.getValue() / 255f;
                        }).row();
                        colors.slider(0, 255, 1, element.b * 255, (ignored) -> {}).update((s) -> {
                            element.b = s.getValue() / 255f;
                        });
                    }).pad(0);
                    table.image().update(img -> {
                        img.setDrawable(((TextureRegionDrawable) img.getDrawable())
                                .tint(element.r, element.g, element.b, 1.0f));
                    }).size(40, 120);
                } else {
                    table.defaults().size(200, 70).pad(6);
                    table.button("Export ship", Icon.export, () -> {}).row();
                    table.button("Import ship", Icon.upload, () -> {});
                }
            };
            menu.setBackground(Styles.black);
            menu.table(pane -> {
                pane.setBackground(Tex.paneRight);
                pane.table(button -> {
                    button.defaults().size(48);
                    button.button(Icon.copy, Styles.cleari, () -> {
                    }).tooltip("Copy ship");
                    button.button(Icon.pencil, Styles.cleari, () -> {
                        element.deletion = false;
                        element.painting = false;
                        rebuild2.run();
                    }).tooltip("Place mod");
                    button.button(Icon.cancel, Styles.cleari, () -> {
                        element.deletion = true;
                        element.painting = false;
                        element.current.part = null;
                        rebuild2.run();
                    }).tooltip("Delete mode");
                    button.button(Icon.rotate, Styles.cleari, () -> {
                        if(element.hasCurrent()) {
                            element.current.rotation = (element.current.rotation + 1) % 4;
                        }
                    }).tooltip("Rotate part");
                    button.row();
                    button.button(new TextureRegionDrawable(asset("mirror-x")), Styles.clearTogglei, () -> {
                        if(mirrorX(element.mirror)) {
                            element.mirror = mirrorY(element.mirror) ? MIRROR_Y : NO_MIRROR;
                        } else {
                            element.mirror = mirrorY(element.mirror) ? MIRROR_XY : MIRROR_X;
                        }
                    }).tooltip("Mirror X");
                    button.button(new TextureRegionDrawable(asset("mirror-y")), Styles.clearTogglei, () -> {
                        if(mirrorY(element.mirror)) {
                            element.mirror = mirrorX(element.mirror) ? MIRROR_X : NO_MIRROR;
                        } else {
                            element.mirror = mirrorX(element.mirror) ? MIRROR_XY : MIRROR_Y;
                        }
                    }).tooltip("Mirror Y");
                    button.button(Icon.move, Styles.cleari, () -> {
                        element.current.part = null;
                        element.deletion = false;
                        element.painting = false;
                        rebuild2.run();
                    }).tooltip("Drag move");
                    button.button(Icon.fill, Styles.cleari, () -> {
                        element.painting = true;
                        element.deletion = false;
                        element.current.part = null;
                        rebuild2.run();
                    }).tooltip("Paint part");
                }).grow().row();
                pane.button("@close", Icon.left, this::hide).size(150, 40);
            }).growY();
            menu.table(pane -> {
                pane.setBackground(Tex.paneRight);
                tableA[0] = pane;
                rebuild2.run();
            }).growY();
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
                                o.add(new Image(asset("default-part-background"))).size(75).scaling(Scaling.fit);
                            }));
                            for(TextureRegion icon : part.drawer.icons()) {
                                stack.add(new Table(o -> {
                                    o.left();
                                    float s = part.width == 1 && part.height == 1 ? 25f : 50f;
                                    o.add(new Image(icon)).size(s).padLeft((75-s)/2f).scaling(Scaling.fit);
                                }));
                            }
                            ClickListener listener = new ClickListener();
                            stack.addListener(listener);
                            stack.addListener(new HandCursorListener());
                            stack.clicked(() -> {
                                element.current.part = part;
                                element.deletion = false;
                                element.painting = false;
                                rebuild2.run();
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
