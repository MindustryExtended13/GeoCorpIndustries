package gmod.ui;

import arc.Core;
import arc.graphics.Pixmap;
import arc.graphics.g2d.TextureRegion;
import arc.scene.event.ClickListener;
import arc.scene.event.HandCursorListener;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Dialog;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import gmod.parts.Part;
import gmod.parts.PartsCategory;
import gmod.schematics.ShipSchematic;
import gmod.util.GeoGroups;
import gmod.world.block.units.SpaceShipConstructor.SpaceShipConstructorBuild;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static gmod.GeoCorp.*;
import static gmod.util.EnumMirror.*;
import static mindustry.Vars.*;

public class PartsEditorDialog extends BaseDialog {
    public SpaceShipConstructorBuild build;
    public PartsCategory partsCategory;
    public PartsEditorElement element;

    private void loadScheme(String str, Runnable runnable) {
        ShipSchematic sc = ShipSchematic.loadSchematic(str);
        if(sc != null) {
            try {
                sc.uploadTo(build);
                runnable.run();
            } catch(Exception e) {
                Vars.ui.showInfo("Failed to load schematic: " + e);
            }
        } else {
            Vars.ui.showInfo("Failed to load schematic");
        }
    }

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
                    table.defaults().size(200, 40).pad(6);
                    table.field(build.shipName, (str) -> {
                        build.shipName = str;
                    }).update(field -> {
                        field.setText(build.shipName);
                    }).valid((str) -> {
                        return !str.trim().isEmpty();
                    }).row();
                    table.button("Export ship", Icon.exportSmall, () -> {
                        Dialog selection = new Dialog("Export ship");
                        selection.cont.defaults().width(350).pad(6);
                        selection.cont.button("[accent]Copy ship to the clipboard[]\nExports " +
                                "ship code to the clipboard text, .json format", Icon.copy, () -> {
                            Core.app.setClipboardText(build.getSchematic().asString());
                            Vars.ui.showInfoToast("@copied", 120f);
                        }).row();
                        selection.cont.button("[accent]Save ship to the file[]\nExports " +
                                "ship code to the json file, for publishing i think", Icon.file, () -> {
                            ShipSchematic schematic = build.getSchematic();
                            Vars.platform.export(schematic.getName(), "json", file -> {
                                file.writeString(schematic.asString(), false, "UTF-8");
                            });
                        }).row();
                        selection.cont.button("Exit", Icon.left, selection::hide);
                        selection.show();
                    }).row();
                    table.button("Import ship", Icon.downloadSmall, () -> {
                        Dialog selection = new Dialog("Import ship");
                        selection.cont.defaults().width(350).pad(6);
                        selection.cont.button("[accent]Import from the file[]\nImports ship " +
                                "from the json file", Icon.file, () -> {
                            Vars.platform.showFileChooser(true, "json", file -> {
                                loadScheme(file.readString("UTF-8"), selection::hide);
                            });
                        }).row();
                        selection.cont.button("[accent]Import from the text[]\nUploads " +
                                "ship from the text, large json text can be used if you " +
                                "don`t want remove copied text", Icon.paste, () -> {
                            Dialog paste = new Dialog("Type text");
                            String[] value = new String[1];
                            paste.cont.field("", (str) -> {
                                value[0] = str;
                            }).size(600, 50).pad(6).row();
                            paste.cont.table(x -> {
                                x.defaults().size(250, 50).pad(6);
                                x.button("Load", Icon.upload, () -> {
                                    loadScheme(value[0], () -> {
                                        paste.hide();
                                        selection.hide();
                                    });
                                });
                                x.button("Exit", Icon.left, paste::hide);
                            });
                            paste.show();
                        }).row();
                        selection.cont.button("[accent]Import from the clipboard[]\nUploads " +
                                "ship from the copied text, if will be loaded" +
                                " last copied text will be removed", Icon.paste, () -> {
                            loadScheme(Core.app.getClipboardText(), selection::hide);
                        }).row();
                        selection.cont.button("Exit", Icon.left, selection::hide);
                        selection.show();
                    });
                }
            };
            menu.setBackground(Styles.black);
            menu.table(pane -> {
                pane.setBackground(Tex.paneRight);
                pane.table(button -> {
                    button.defaults().size(48);
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
                    button.button(Icon.move, Styles.cleari, () -> {
                        element.current.part = null;
                        element.deletion = false;
                        element.painting = false;
                        rebuild2.run();
                    }).tooltip("Drag move");
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
