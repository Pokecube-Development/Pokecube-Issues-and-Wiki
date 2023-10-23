package pokecube.core.client.gui.animation;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.client.gui.AnimationGui;
import pokecube.core.client.gui.helper.ListEditBox;
import pokecube.core.client.render.mobs.RenderPokemob;
import thut.api.entity.animation.IAnimationChanger.WornOffsets;
import thut.api.maths.Vector3;
import thut.bling.BlingItem;
import thut.core.client.render.animation.AnimationChanger;
import thut.core.client.render.animation.AnimationLoader;
import thut.core.client.render.model.parts.Mesh;
import thut.lib.RegHelper;
import thut.lib.TComponent;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

public class WornOffsetModule extends AnimModule
{
    EditBox worn_slot;
    EditBox worn_part;
    EditBox worn_item;
    EditBox rX;
    EditBox rY;
    EditBox rZ;
    EditBox dX;
    EditBox dY;
    EditBox dZ;
    EditBox scaleS;

    EditBox test_animation;

    Map<String, EnumWearable> wearableNames = EnumWearable.wearableNames;
    Map<String, Integer> slots = EnumWearable.slotsNames;
    List<String> sortedSlots = Lists.newArrayList();
    int worn_index = 0;

    RenderPokemob renderer = null;

    public WornOffsetModule(AnimationGui parent)
    {
        super(parent);
        sortedSlots.addAll(slots.keySet());
        sortedSlots.sort(null);
    }

    private void resetWearableValues()
    {
        String key = worn_slot.getValue();
        EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
        Object ren = manager.getRenderer(parent.toRender.getEntity());

        if (ren instanceof RenderPokemob renderer
                && renderer.getModel().renderer.getAnimationChanger() instanceof AnimationChanger changer)
        {
            WornOffsets old = changer.wornOffsets.get(key);
            if (old != null)
            {
                this.worn_part.setValue(old.parent);
                dX.setValue(String.format(Locale.ROOT, "%.3f", old.offset.x));
                dY.setValue(String.format(Locale.ROOT, "%.3f", old.offset.y));
                dZ.setValue(String.format(Locale.ROOT, "%.3f", old.offset.z));

                rX.setValue(String.format(Locale.ROOT, "%.1f", old.angles.x));
                rY.setValue(String.format(Locale.ROOT, "%.1f", old.angles.y));
                rZ.setValue(String.format(Locale.ROOT, "%.1f", old.angles.z));

                double sx = old.scale.x;
                double sy = old.scale.y;
                double sz = old.scale.z;

                if (sx == sy && sy == sz) scaleS.setValue(String.format(Locale.ROOT, "%.3f", sx));
                else scaleS.setValue("%.3f,%.3f,%.3f".formatted(sx, sy, sz));
            }
        }

        final PlayerWearables wearables = ThutWearables.getWearables(parent.toRender.getEntity());
        for (final EnumWearable w : EnumWearable.values()) if (w.slots == 2)
        {
            wearables.setWearable(w, ItemStack.EMPTY, 0);
            wearables.setWearable(w, ItemStack.EMPTY, 1);
        }
        else
        {
            wearables.setWearable(w, ItemStack.EMPTY, 0);
        }

        ItemStack stack = ItemStack.EMPTY;
        EnumWearable slot = wearableNames.get(key);
        if (stack.isEmpty())
        {
            for (Item i2 : BlingItem.bling)
            {
                if (BlingItem.defaults.get(i2) == slot)
                {
                    stack = new ItemStack(i2);
                    break;
                }
            }
        }
        wearables.setWearable(slot, stack.copy(), slots.get(key));
        if (!stack.isEmpty())
        {
            this.worn_item.setValue(RegHelper.getKey(stack) + "");
        }
    }

    private void updateWearableRender()
    {
        String key = worn_slot.getValue();
        String part = worn_part.getValue();
        String worn = worn_item.getValue();

        final PlayerWearables wearables = ThutWearables.getWearables(parent.toRender.getEntity());
        for (final EnumWearable w : EnumWearable.values()) if (w.slots == 2)
        {
            wearables.setWearable(w, ItemStack.EMPTY, 0);
            wearables.setWearable(w, ItemStack.EMPTY, 1);
        }
        else
        {
            wearables.setWearable(w, ItemStack.EMPTY, 0);
        }

        Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(worn));
        ItemStack stack = new ItemStack(i);
        EnumWearable slot = wearableNames.get(key);
        if (stack.isEmpty())
        {
            for (Item i2 : BlingItem.bling)
            {
                if (BlingItem.defaults.get(i2) == slot)
                {
                    stack = new ItemStack(i2);
                    break;
                }
            }
        }
        wearables.setWearable(slot, stack, slots.get(key));

        EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
        Object ren = manager.getRenderer(parent.toRender.getEntity());
        if (ren instanceof RenderPokemob renderer
                && renderer.getModel().renderer.getAnimationChanger() instanceof AnimationChanger changer)
        {
            Vector3 w_offset = new Vector3(Float.parseFloat(dX.getValue()), Float.parseFloat(dY.getValue()),
                    Float.parseFloat(dZ.getValue()));
            Vector3 w_angles = new Vector3(Float.parseFloat(rX.getValue()), Float.parseFloat(rY.getValue()),
                    Float.parseFloat(rZ.getValue()));
            Vector3 w_scale = AnimationLoader.getVector3(this.scaleS.getValue(), null);
            WornOffsets replace = new WornOffsets(part, w_offset, w_scale, w_angles);
            changer.wornOffsets.put(key, replace);
        }
    }

    @Override
    public void setEnabled(boolean active)
    {
        super.setEnabled(active);
        if (this.active) this.resetWearableValues();
    }

    @Override
    public void onUpdated()
    {
        if (test_animation.isFocused()) return;
        if (!slots.containsKey(worn_slot.getValue()))
        {
            worn_slot.setValue(sortedSlots.get(worn_index));
            resetWearableValues();
        }
        else
        {
            try
            {
                updateWearableRender();
            }
            catch (Exception err)
            {
                err.printStackTrace();
            }
        }
    }

    @Override
    public void preRender()
    {
        renderer = null;
        EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
        Object ren = manager.getRenderer(parent.toRender.getEntity());
        if (ren instanceof RenderPokemob ren2) renderer = ren2;
        if (renderer != null && isPauseScreen())
        {
            renderer.getModel().debugMode = true;
            Mesh.debug = true;
            for (var part : renderer.getModel().getParts().values())
            {
                if (part.getName().equals(worn_part.getValue())) part.setRGBABrO(255, 0, 0, 155, 15728880, 0);
                else part.setRGBABrO(255, 255, 255, 55, 15728880, 0);
            }
        }
    }

    @Override
    public void postRender()
    {
        if (renderer != null)
        {
            renderer.getModel().debugMode = false;
            Mesh.debug = false;
        }
    }

    @Override
    public boolean isPauseScreen()
    {
        return parent.toRender.getEntity().getPersistentData().getBoolean("__offset_debug_menu__");
    }

    @Override
    public boolean updateOnButtonPress(int code)
    {
        if (code == GLFW.GLFW_KEY_UP || code == GLFW.GLFW_KEY_DOWN)
        {
            upDownPressed(code);
            return true;
        }
        if ((code == GLFW.GLFW_KEY_ENTER || code == GLFW.GLFW_KEY_KP_ENTER) && test_animation.isFocused()
                && !worn_part.getValue().isBlank())
        {
            if (test_animation.getValue().isBlank()) parent.testAnimation = "";
            else
            {
                String args = "f::" + worn_part.getValue() + "::" + test_animation.getValue();
                parent.testAnimation = args;
            }
        }
        return false;
    }

    @Override
    public void init()
    {
        int yOffset = parent.height / 2;

        final Component zero = TComponent.literal("0");
        final Component one = TComponent.literal("1");

        final Component reset = TComponent.literal("reset");
        final Component right = TComponent.literal("\u25b6");
        final Component left = TComponent.literal("\u25c0");

        yOffset -= yOffset / 2;
        yOffset += 57;
        final Component blank = TComponent.literal("");

        int dx = parent.width - 210;
        this.worn_item = new ListEditBox(parent.font, dx, yOffset - 90, 100, 10, blank);
        this.worn_slot = new ListEditBox(parent.font, dx, yOffset - 80, 100, 10, blank);
        this.worn_part = new ListEditBox(parent.font, dx, yOffset - 70, 100, 10, blank);

        this.test_animation = new ListEditBox(parent.font, dx, yOffset - 00, 100, 10, blank);

        yOffset += 10;
        this.scaleS = new ListEditBox(parent.font, dx, yOffset - 50, 100, 10, one);
        this.rZ = new ListEditBox(parent.font, dx, yOffset - 20, 50, 10, zero);
        this.rY = new ListEditBox(parent.font, dx, yOffset - 30, 50, 10, zero);
        this.rX = new ListEditBox(parent.font, dx, yOffset - 40, 50, 10, zero);
        dx += 50;
        this.dZ = new ListEditBox(parent.font, dx, yOffset - 20, 50, 10, zero);
        this.dY = new ListEditBox(parent.font, dx, yOffset - 30, 50, 10, zero);
        this.dX = new ListEditBox(parent.font, dx, yOffset - 40, 50, 10, zero);
        yOffset -= 10;

        final Component copy = TComponent.literal("copy");
        dx -= 45;
        this.addRenderableWidget(new Button(dx, yOffset - 60, 30, 10, copy, b2 -> {
            String xml = "\n  <worn id=\"%s\" parent=\"%s\" offset=\"%s,%s,%s\" angles=\"%s,%s,%s\" scale=\"%s\"/>";
            String key = this.worn_slot.getValue();
            String part = this.worn_part.getValue();

            Function<String, String> trim = e -> {
                String var = e;
                while (var.endsWith("0") && var.contains("."))
                {
                    var = var.substring(0, var.length() - 1);
                }
                if (var.endsWith(".")) var = var.substring(0, var.length() - 1);
                return var;
            };
            String dX = trim.apply(this.dX.getValue());
            String dY = trim.apply(this.dY.getValue());
            String dZ = trim.apply(this.dZ.getValue());

            String rX = trim.apply(this.rX.getValue());
            String rY = trim.apply(this.rY.getValue());
            String rZ = trim.apply(this.rZ.getValue());

            String scale = scaleS.value;
            Vector3 v = AnimationLoader.getVector3(scale, Vector3.empty);
            if (v.x == v.y && v.y == v.z) scale = trim.apply(String.format(Locale.ROOT, "%.3f", v.x));
            else
            {
                String xs = trim.apply(String.format(Locale.ROOT, "%.3f", v.x));
                String ys = trim.apply(String.format(Locale.ROOT, "%.3f", v.y));
                String zs = trim.apply(String.format(Locale.ROOT, "%.3f", v.z));
                scale = "%s,%s,%s".formatted(xs, ys, zs);
            }
            xml = xml.formatted(key, part, dX, dY, dZ, rX, rY, rZ, scale);
            Minecraft.getInstance().keyboardHandler.setClipboard(xml);
            Minecraft.getInstance().player.displayClientMessage(TComponent.literal("Copied XML to clipboard"), true);
        }));
        dx += 30;
        this.addRenderableWidget(new Button(dx, yOffset - 60, 30, 10, reset, b2 -> {
            resetWearableValues();
        }));
        dx += 30;
        this.addRenderableWidget(new Button(dx, yOffset - 60, 30, 10, TComponent.literal("SEE"), b -> {
            boolean pause = parent.toRender.getEntity().getPersistentData().getBoolean("__offset_debug_menu__");
            parent.toRender.getEntity().getPersistentData().putBoolean("__offset_debug_menu__", !pause);
        }));

        dx = parent.width - 220;
        int dy = -80;
        this.addRenderableWidget(new Button(dx, yOffset + dy, 10, 10, right, b2 -> {
            this.worn_index++;
            this.worn_index = this.worn_index % sortedSlots.size();
            worn_slot.setValue(sortedSlots.get(worn_index));
            this.resetWearableValues();
        }));
        dx -= 10;
        this.addRenderableWidget(new Button(dx, yOffset + dy, 10, 10, left, b2 -> {
            this.worn_index--;
            if (worn_index < 0) worn_index = sortedSlots.size() - 1;
            this.worn_index = this.worn_index % sortedSlots.size();
            worn_slot.setValue(sortedSlots.get(worn_index));
            this.resetWearableValues();
        }));

        dx = parent.width - 220;
        dy = -70;
        this.addRenderableWidget(new Button(dx, yOffset + dy, 10, 10, right, b2 -> {
            List<String> renders = Lists.newArrayList();
            EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
            Object ren = manager.getRenderer(parent.toRender.getEntity());
            if (ren instanceof RenderPokemob renderer)
            {
                renderer.getModel().getParts().forEach((s, p) -> {
                    if (!sortedSlots.contains(s) && !p.getMaterials().isEmpty()) renders.add(s);
                });
                renders.sort(null);
                int index = renders.indexOf(this.worn_part.getValue());
                if (index == -1)
                {
                    this.worn_part.setValue(renders.get(0));
                }
                else
                {
                    index++;
                    this.worn_part.setValue(renders.get(index % renders.size()));
                }
            }
        }));
        dx -= 10;
        this.addRenderableWidget(new Button(dx, yOffset + dy, 10, 10, left, b2 -> {
            List<String> renders = Lists.newArrayList();
            EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
            Object ren = manager.getRenderer(parent.toRender.getEntity());
            if (ren instanceof RenderPokemob renderer)
            {
                renderer.getModel().getParts().forEach((s, p) -> {
                    if (!sortedSlots.contains(s) && !p.getMaterials().isEmpty()) renders.add(s);
                });
                renders.sort(null);
                int index = renders.indexOf(this.worn_part.getValue());
                if (index == -1)
                {
                    this.worn_part.setValue(renders.get(0));
                }
                else
                {
                    index--;
                    if (index < 0) index = renders.size() - 1;
                    this.worn_part.setValue(renders.get(index % renders.size()));
                }
            }
        }));

        this.rX.setValue("0");
        this.rY.setValue("0");
        this.rZ.setValue("0");

        this.dX.setValue("0");
        this.dY.setValue("0");
        this.dZ.setValue("0");
        this.scaleS.setValue("1");

        this.worn_slot.setValue(this.sortedSlots.get(0));
        this.worn_slot.isEditable = false;

        this.addRenderableWidget(this.worn_item);
        this.addRenderableWidget(this.worn_part);
        this.addRenderableWidget(this.worn_slot);
        this.addRenderableWidget(this.rX);
        this.addRenderableWidget(this.rY);
        this.addRenderableWidget(this.rZ);
        this.addRenderableWidget(this.dX);
        this.addRenderableWidget(this.dY);
        this.addRenderableWidget(this.dZ);
        this.addRenderableWidget(this.scaleS);
        this.addRenderableWidget(this.test_animation);

        this.setEnabled(false);
    }

    private void upDownPressed(int code)
    {
        float dv = code == GLFW.GLFW_KEY_UP ? 1 : -1;
        if (Screen.hasShiftDown()) dv *= 0.1f;
        if (Screen.hasControlDown()) dv *= 10f;

        if (rX.isFocused())
        {
            try
            {
                float value = Float.parseFloat(rX.value);
                value += dv;
                rX.setValue(String.format(Locale.ROOT, "%.1f", value));
            }
            catch (Exception e)
            {
                rX.setValue("0");
            }

        }
        else if (rY.isFocused())
        {

            try
            {
                float value = Float.parseFloat(rY.value);
                value += dv;
                rY.setValue(String.format(Locale.ROOT, "%.1f", value));
            }
            catch (Exception e)
            {
                rY.setValue("0");
            }
        }
        else if (rZ.isFocused())
        {
            try
            {
                float value = Float.parseFloat(rZ.value);
                value += dv;
                rZ.setValue(String.format(Locale.ROOT, "%.1f", value));
            }
            catch (Exception e)
            {
                rZ.setValue("0");
            }
        }

        if (dX.isFocused())
        {
            try
            {
                float value = Float.parseFloat(dX.value);
                value += 0.01f * dv;
                dX.setValue(String.format(Locale.ROOT, "%.3f", value));
            }
            catch (Exception e)
            {
                dX.setValue("0");
            }
        }
        else if (dY.isFocused())
        {

            try
            {
                float value = Float.parseFloat(dY.value);
                value += 0.01f * dv;
                dY.setValue(String.format(Locale.ROOT, "%.3f", value));
            }
            catch (Exception e)
            {
                dY.setValue("0");
            }
        }
        else if (dZ.isFocused())
        {
            try
            {
                float value = Float.parseFloat(dZ.value);
                value += 0.01f * dv;
                dZ.setValue(String.format(Locale.ROOT, "%.3f", value));
            }
            catch (Exception e)
            {
                dZ.setValue("0");
            }
        }
        else if (scaleS.isFocused() && !scaleS.getValue().contains(","))
        {
            try
            {
                float value = Float.parseFloat(scaleS.value);
                value += 0.01f * dv;
                scaleS.setValue(String.format(Locale.ROOT, "%.3f", value));
            }
            catch (Exception e)
            {
                scaleS.setValue("1");
            }
        }
    }
}