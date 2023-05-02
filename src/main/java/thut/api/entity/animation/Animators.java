package thut.api.entity.animation;

import java.util.ArrayList;
import java.util.List;

import org.nfunk.jep.JEP;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.IAnimated.MolangVars;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.common.ThutCore;

public class Animators
{
    public static interface IAnimator
    {
        boolean animate(Animation animation, IAnimationHolder holder, IExtendedModelPart part, float partialTick,
                float limbSwing, int tick);

        default void setColours(int... rgba)
        {}

        int getLength();

        boolean hasLimbBased();

        void setLimbBased();

        void setHidden(boolean hidden);
    }

    public static class MixedAnimator implements IAnimator
    {
        List<IAnimator> anims = new ArrayList<>();
        int length = -1;
        boolean limbBased = false;

        public MixedAnimator(List<IAnimator> anims)
        {
            this.anims.addAll(anims);
            for (var anim : anims)
            {
                length = Math.max(length, anim.getLength());
                limbBased |= anim.hasLimbBased();
            }
        }

        @Override
        public boolean animate(Animation animation, IAnimationHolder holder, IExtendedModelPart part, float partialTick,
                float limbSwing, int tick)
        {
            boolean animated = false;
            for (var anim : anims)
                animated = anim.animate(animation, holder, part, partialTick, limbSwing, tick) | animated;
            return animated;
        }

        @Override
        public int getLength()
        {
            return length;
        }

        @Override
        public boolean hasLimbBased()
        {
            return limbBased;
        }

        @Override
        public void setLimbBased()
        {
            for (var anim : anims) anim.setLimbBased();
        }

        @Override
        public void setHidden(boolean hidden)
        {
            for (var anim : anims) anim.setHidden(hidden);
        }
    }

    private static final Vector4 _rot = new Vector4();

    public static class KeyframeAnimator implements IAnimator
    {
        float[] dr = new float[3];
        float[] ds = new float[3];
        float[] dx = new float[3];

        private int[] colours =
        { -1, -1, -1, -1 };

        public final List<AnimationComponent> components;
        private int length = -1;
        private boolean limbBased;
        private boolean hidden = false;

        public KeyframeAnimator(List<AnimationComponent> components)
        {
            this.components = components;
            this.length = -1;
            this.limbBased = false;
            for (final AnimationComponent component : components)
            {
                this.length = Math.max(this.length, component.startKey + component.length);
                this.limbBased = this.limbBased || component.limbBased;
            }
        }

        public boolean animateJEP(Animation animation, AnimationComponent component, IExtendedModelPart part,
                MolangVars molangs)
        {
            if (component._foundNoJEP) return false;
            if (hidden)
            {
                part.setHidden(true);
                return true;
            }
            if (colours[0] != -1)
            {
                part.setRGBABrO(null, colours[0], colours[1], colours[2], colours[3], Integer.MIN_VALUE, -1);
            }
            int modifies = 0;
            for (int i = 0; i < 3; i++)
            {
                dr[i] = 0;
                ds[i] = 1;
                dx[i] = 0;
                if (component._rotFunctions[i] != null)
                {
                    molangs.updateJEP(component._rotFunctions[i]);
                    dr[i] = (float) component._rotFunctions[i].getValue();
                    modifies++;
                }
                if (component._scaleFunctions[i] != null)
                {
                    molangs.updateJEP(component._scaleFunctions[i]);
                    ds[i] = (float) component._scaleFunctions[i].getValue();
                    modifies++;
                }
                if (component._posFunctions[i] != null)
                {
                    molangs.updateJEP(component._posFunctions[i]);
                    dx[i] = (float) component._posFunctions[i].getValue() / 16f;
                    modifies++;
                }
            }
            component._foundNoJEP = modifies == 0;
            return !component._foundNoJEP;
        }

        @Override
        public boolean animate(Animation animation, IAnimationHolder holder, IExtendedModelPart part, float partialTick,
                float limbSwing, int tick)
        {
            boolean animated = false;
            final Vector3 temp = animation._shift.clear();
            float rx = 0, ry = 0, rz = 0;
            float sx = 1, sy = 1, sz = 1;
            int aniTick = tick;
            float time1 = aniTick;
            float time2 = 0;
            int animationLength = animation.getLength();
            MolangVars molangs = holder.getMolangVars();
            animationLength = Math.max(1, animationLength);
            
            final float limbSpeedFactor = 3f;
            time1 = (time1 + partialTick) % animationLength;
            time2 = limbSwing * limbSpeedFactor % animationLength;

            time1 = (float) (molangs.t % animationLength);
            time2 = (float) (molangs.l % animationLength);
            
            
            aniTick = (int) time1;

            for (final AnimationComponent component : components)
            {
                final float time = component.limbBased ? time2 : time1;
                if (component.limbBased) aniTick = (int) time2;
                if (time >= component.startKey)
                {
                    // Start by checking JEP components to the animation
                    boolean jepAnimated = animateJEP(animation, component, part, molangs);
                    if (jepAnimated)
                    {
                        rx += dr[0];
                        ry += dr[1];
                        rz += dr[2];

                        temp.addTo(dx[0], dx[1], dx[2]);

                        sx *= ds[0];
                        sy *= ds[1];
                        sz *= ds[2];
                    }
                    animated = true;
                    float componentTimer = time - component.startKey;
                    if (componentTimer > component.length) componentTimer = component.length;
                    final int length = component.length == 0 ? 1 : component.length;
                    final float ratio = componentTimer / length;
                    temp.addTo(component.posChange[0] * ratio + component.posOffset[0],
                            component.posChange[1] * ratio + component.posOffset[1],
                            component.posChange[2] * ratio + component.posOffset[2]);
                    rx += (float) (component.rotChange[0] * ratio + component.rotOffset[0]);
                    ry += (float) (component.rotChange[1] * ratio + component.rotOffset[1]);
                    rz += (float) (component.rotChange[2] * ratio + component.rotOffset[2]);

                    sx += (float) (component.scaleChange[0] * ratio + component.scaleOffset[0]);
                    sy += (float) (component.scaleChange[1] * ratio + component.scaleOffset[1]);
                    sz += (float) (component.scaleChange[2] * ratio + component.scaleOffset[2]);

                    // Apply hidden like this so last hidden state is kept
                    part.setHidden(component.hidden);
                }
            }
            holder.setStep(animation, aniTick + 2);
            if (animated)
            {
                part.setPreTranslations(temp);
                part.setPreScale(temp.set(sx, sy, sz));
                final Quaternion quat = new Quaternion(0, 0, 0, 1);
                if (rz != 0) quat.mul(Vector3f.YN.rotationDegrees(rz));
                if (rx != 0) quat.mul(Vector3f.XP.rotationDegrees(rx));
                if (ry != 0) quat.mul(Vector3f.ZP.rotationDegrees(ry));
                part.setPreRotations(_rot.set(quat));
            }
            return animated;
        }

        @Override
        public int getLength()
        {
            return this.length;
        }

        @Override
        public boolean hasLimbBased()
        {
            return limbBased;
        }

        @Override
        public void setLimbBased()
        {
            limbBased = true;
        }

        @Override
        public void setHidden(boolean hidden)
        {
            this.hidden = hidden;
            for (final AnimationComponent component : components) component.hidden = hidden;
        }
    }

    public static class FunctionAnimation implements IAnimator
    {

        public static void fillJEPs(JEP[] jeps, String _funcs)
        {
            String[] funcs = _funcs.split(",");
            func:
            for (String s : funcs)
            {
                String[] args = s.split(":");
                int i;
                switch (args[0])
                {
                case ("x"):
                    i = 0;
                    break;
                case ("y"):
                    i = 1;
                    break;
                case ("z"):
                    i = 2;
                    break;
                case ("rx"):
                    i = 0;
                    break;
                case ("ry"):
                    i = 1;
                    break;
                case ("rz"):
                    i = 2;
                    break;
                case ("dx"):
                    i = 0;
                    break;
                case ("dy"):
                    i = 1;
                    break;
                case ("dz"):
                    i = 2;
                    break;
                default:
                    ThutCore.LOGGER.error("Malformed function animation {}", s);
                    continue func;
                }
                jeps[i] = new JEP();
                jeps[i].addStandardFunctions();
                jeps[i].addStandardConstants();
                jeps[i].addVariable("t", 0);
                jeps[i].addVariable("l", 0);
                jeps[i].parseExpression(args[1]);
            }
        }

        public static FunctionAnimation makeRotationTest(String rotations)
        {
            JEP[] rots = new JEP[3];
            FunctionAnimation.fillJEPs(rots, rotations);
            return new FunctionAnimation(rots);
        }

        public static FunctionAnimation makeOffsetTest(String offsets)
        {
            JEP[] offs = new JEP[3];
            FunctionAnimation.fillJEPs(offs, offsets);
            return new FunctionAnimation(new JEP[3], offs);
        }

        public JEP[] rotFunctions;
        public JEP[] posFunctions;
        public JEP[] scaleFunctions;

        private boolean hidden;
        private boolean limbBased;

        float[] dr = new float[3];
        float[] ds = new float[3];
        float[] dx = new float[3];

        private int[] colours =
        { -1, -1, -1, -1 };

        public FunctionAnimation(JEP[] rotFunctions)
        {
            this.rotFunctions = rotFunctions;
            this.posFunctions = new JEP[3];
            this.scaleFunctions = new JEP[3];
        }

        public FunctionAnimation(JEP[] rotFunctions, JEP[] posFunctions)
        {
            this.rotFunctions = rotFunctions;
            this.posFunctions = posFunctions;
            this.scaleFunctions = new JEP[3];
        }

        public FunctionAnimation(JEP[] rotFunctions, JEP[] posFunctions, JEP[] scaleFunctions)
        {
            this.rotFunctions = rotFunctions;
            this.posFunctions = posFunctions;
            this.scaleFunctions = scaleFunctions;
        }

        @Override
        public boolean animate(Animation animation, IAnimationHolder holder, IExtendedModelPart part, float partialTick,
                float limbSwing, int tick)
        {
            if (hidden)
            {
                part.setHidden(true);
                return true;
            }
            int aniTick = tick;
            float time1 = aniTick;
            float time2 = 0;
            final float limbSpeedFactor = 3f;
            time1 = (time1 + partialTick);
            time2 = limbSwing * limbSpeedFactor;
            aniTick = (int) time1;

            final Vector3 temp = animation._shift.clear();
            if (colours[0] != -1)
            {
                part.setRGBABrO(null, colours[0], colours[1], colours[2], colours[3], Integer.MIN_VALUE, -1);
            }
            for (int i = 0; i < 3; i++)
            {
                dr[i] = 0;
                ds[i] = 1;
                dx[i] = 0;
                if (rotFunctions[i] != null)
                {
                    rotFunctions[i].setVarValue("t", time1);
                    rotFunctions[i].setVarValue("l", time2);
                    dr[i] = (float) rotFunctions[i].getValue();
                }
                if (scaleFunctions[i] != null)
                {
                    scaleFunctions[i].setVarValue("t", time1);
                    scaleFunctions[i].setVarValue("l", time2);
                    ds[i] = (float) scaleFunctions[i].getValue();
                }
                if (posFunctions[i] != null)
                {
                    posFunctions[i].setVarValue("t", time1);
                    posFunctions[i].setVarValue("l", time2);
                    dx[i] = (float) posFunctions[i].getValue() / 16f;
                }
            }
            part.setPreTranslations(temp.set(dx));
            part.setPreScale(temp.set(ds));
            final Quaternion quat = new Quaternion(0, 0, 0, 1);
            if (dr[2] != 0) quat.mul(Vector3f.YN.rotationDegrees(dr[2]));
            if (dr[0] != 0) quat.mul(Vector3f.XP.rotationDegrees(dr[0]));
            if (dr[1] != 0) quat.mul(Vector3f.ZP.rotationDegrees(dr[1]));
            part.setPreRotations(_rot.set(quat));
            return true;
        }

        @Override
        public int getLength()
        {
            return 0;
        }

        @Override
        public boolean hasLimbBased()
        {
            return limbBased;
        }

        @Override
        public void setLimbBased()
        {
            limbBased = true;
        }

        @Override
        public void setHidden(boolean hidden)
        {
            this.hidden = hidden;
        }
    }
}
