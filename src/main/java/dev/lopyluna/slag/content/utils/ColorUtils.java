package dev.lopyluna.slag.content.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.joml.Vector3d;
import org.joml.Vector4d;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import static com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA;
import static com.mojang.blaze3d.platform.GlStateManager.SourceFactor.DST_COLOR;
import static net.minecraft.client.renderer.RenderStateShard.*;
import static org.lwjgl.opengl.GL14.*;

@SuppressWarnings("unused")
public class ColorUtils {

    public enum ColorBlendType {
        AVERAGE,
        BLEND,
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE,
        SCREEN,
        OVERLAY,
        DARKEN,
        LIGHTEN,
        DIFFERENCE,
        NEGATION,
        INVERT,
        INVERT_COLOR,
        INVERT_SHADE,
        INVERT_HUE,
        EXCLUSION,
        HARD_LIGHT,
        SOFT_LIGHT,
        COLOR_BURN,
        COLOR_DODGE,
        GLOW,
        REFLECT,
        XOR,
        VIVID_LIGHT,
        LINEAR_LIGHT,
        PIN_LIGHT
    }

    public static Color toBlend(Color valueA, Color valueB, ColorBlendType type) {
        return valueA.toBlend(valueB, type);
    }

    public abstract static class Color {
        public abstract int toHex();
        public abstract String toHexString();
        public abstract Vector3d toVec3();
        public abstract Vector4d toVec4();
        public abstract RYB toRYB();
        public abstract RYBA toRYBA();
        public abstract RGB toRGB();
        public abstract RGBA toRGBA();
        public abstract HSV toHSV();
        public abstract HSVA toHSVA();
        public abstract HSL toHSL();
        public abstract HSLA toHSLA();
        public abstract HWB toHWB();
        public abstract HWBA toHWBA();
        public abstract CMYK toCMYK();
        public abstract CMYKA toCMYKA();
        public abstract String toShortString(); //Ex "r0.48, g0.09, b0.8"
        public abstract String toShortString255(); //Ex "r124, g24, b204"
        public abstract String toString(); //Ex "Red 0.486, Green 0.094, Blue 0.8"
        public abstract String toString255(); //Ex "Red 124, Green 24, Blue 204"
        public abstract boolean equal(Color other);

        public Color toBlend(Color value, ColorBlendType type) {
            return switch (type) {
                case AVERAGE -> toAverage(value);
                case BLEND -> toMerge(value);
                case ADD -> toAdd(value);
                case SUBTRACT -> toSubtract(value);
                case MULTIPLY -> toMultiply(value);
                case DIVIDE -> toDivide(value);
                case SCREEN -> toScreen(value);
                case OVERLAY -> toOverlay(value);
                case DARKEN -> toDarken(value);
                case LIGHTEN -> toLighten(value);
                case DIFFERENCE -> toDifference(value);
                case NEGATION -> toNegation(value);
                case INVERT -> toInvert();
                case INVERT_COLOR -> toInvertColor();
                case INVERT_SHADE -> toInvertShade();
                case INVERT_HUE -> toInvertHue();
                case EXCLUSION -> toExclusion(value);
                case HARD_LIGHT -> toHardLight(value);
                case SOFT_LIGHT -> toSoftLight(value);
                case COLOR_BURN -> toColorBurn(value);
                case COLOR_DODGE -> toColorDodge(value);
                case GLOW -> toGlow(value);
                case REFLECT -> toReflect(value);
                case XOR -> toXor(value);
                case VIVID_LIGHT -> toVividLight(value);
                case LINEAR_LIGHT -> toLinearLight(value);
                case PIN_LIGHT -> toPinLight(value);
            };
        }

        private Color toAverage(Color value) {
            if (value == null) return this;

            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();

            double rN = (aC.r + bC.r) / 2.0;
            double gN = (aC.g + bC.g) / 2.0;
            double bN = (aC.b + bC.b) / 2.0;
            double aN = (aC.a + bC.a) / 2.0;

            return new RGBA(rN, gN, bN, aN);
        }

        private Color toMerge(Color value) {
            if (value == null) return this;

            RGBA src = value.toRGBA();
            RGBA dst = this.toRGBA();

            double outA = src.a + dst.a * (1 - src.a);
            if (outA < 1e-6) return new RGBA(0, 0, 0, 0);

            double outR = (src.r * src.a + dst.r * dst.a * (1 - src.a)) / outA;
            double outG = (src.g * src.a + dst.g * dst.a * (1 - src.a)) / outA;
            double outB = (src.b * src.a + dst.b * dst.a * (1 - src.a)) / outA;

            return new RGBA(outR, outG, outB, outA);
        }


        private Color toAdd(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();

            double rN = Math.min(1.0, aC.r + bC.r);
            double gN = Math.min(1.0, aC.g + bC.g);
            double bN = Math.min(1.0, aC.b + bC.b);
            double aN = Math.min(1.0, aC.a + bC.a);

            return new RGBA(rN, gN, bN, aN);
        }

        private Color toSubtract(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();

            double rN = Math.max(0.0, aC.r - bC.r);
            double gN = Math.max(0.0, aC.g - bC.g);
            double bN = Math.max(0.0, aC.b - bC.b);
            double aN = Math.max(0.0, aC.a - bC.a);

            return new RGBA(rN, gN, bN, aN);
        }

        private Color toMultiply(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();

            double rN = aC.r * bC.r;
            double gN = aC.g * bC.g;
            double bN = aC.b * bC.b;
            double aN = aC.a * bC.a;

            return new RGBA(rN, gN, bN, aN);
        }

        private Color toDivide(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();

            double rN = (bC.r == 0) ? 1.0 : aC.r / bC.r;
            double gN = (bC.g == 0) ? 1.0 : aC.g / bC.g;
            double bN = (bC.b == 0) ? 1.0 : aC.b / bC.b;
            double aN = (bC.a == 0) ? 1.0 : aC.a / bC.a;

            rN = Math.clamp(rN, 0.0, 1.0);
            gN = Math.clamp(gN, 0.0, 1.0);
            bN = Math.clamp(bN, 0.0, 1.0);
            aN = Math.clamp(aN, 0.0, 1.0);

            return new RGBA(rN, gN, bN, aN);
        }

        private Color toScreen(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();

            double rN = 1 - (1 - aC.r) * (1 - bC.r);
            double gN = 1 - (1 - aC.g) * (1 - bC.g);
            double bN = 1 - (1 - aC.b) * (1 - bC.b);
            double aN = 1 - (1 - aC.a) * (1 - bC.a);

            return new RGBA(rN, gN, bN, aN);
        }

        private Color toOverlay(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();

            double rN = (aC.r < 0.5) ? (2 * aC.r * bC.r) : (1 - 2 * (1 - aC.r) * (1 - bC.r));
            double gN = (aC.g < 0.5) ? (2 * aC.g * bC.g) : (1 - 2 * (1 - aC.g) * (1 - bC.g));
            double bN = (aC.b < 0.5) ? (2 * aC.b * bC.b) : (1 - 2 * (1 - aC.b) * (1 - bC.b));
            double aN = (aC.a < 0.5) ? (2 * aC.a * bC.a) : (1 - 2 * (1 - aC.a) * (1 - bC.a));

            return new RGBA(rN, gN, bN, aN);
        }

        private Color toDarken(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();
            return new RGBA(
                    Math.min(aC.r, bC.r),
                    Math.min(aC.g, bC.g),
                    Math.min(aC.b, bC.b),
                    Math.min(aC.a, bC.a)
            );
        }

        private Color toLighten(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();
            return new RGBA(
                    Math.max(aC.r, bC.r),
                    Math.max(aC.g, bC.g),
                    Math.max(aC.b, bC.b),
                    Math.max(aC.a, bC.a)
            );
        }

        private Color toDifference(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();
            return new RGBA(
                    Math.abs(aC.r - bC.r),
                    Math.abs(aC.g - bC.g),
                    Math.abs(aC.b - bC.b),
                    Math.abs(aC.a - bC.a)
            );
        }

        private Color toNegation(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();
            return new RGBA(
                    1 - Math.abs(1 - aC.r - bC.r),
                    1 - Math.abs(1 - aC.g - bC.g),
                    1 - Math.abs(1 - aC.b - bC.b),
                    1 - Math.abs(1 - aC.a - bC.a)
            );
        }

        private Color toInvert() {
            RGBA aC = this.toRGBA();
            return new RGBA(1 - aC.r, 1 - aC.g, 1 - aC.b, aC.a);
        }

        private Color toInvertColor() {
            RGBA aC = this.toRGBA();
            return new RGBA(1 - aC.r, 1 - aC.g, 1 - aC.b, 1 - aC.a);
        }

        private Color toInvertShade() {
            RGB rgb = this.toRGB();
            double gray = (rgb.r + rgb.g + rgb.b) / 3.0;
            double inv = 1 - gray;
            double factor = (gray == 0) ? 0 : inv / gray;
            return new RGBA(
                    Math.clamp(rgb.r * factor, 0, 1),
                    Math.clamp(rgb.g * factor, 0, 1),
                    Math.clamp(rgb.b * factor, 0, 1),
                    this.toRGBA().a
            );
        }

        private Color toInvertHue() {
            HSV hsv = this.toHSV();
            double newHue = (hsv.h + 180.0) % 360.0;
            HSV inverted = new HSV(newHue, hsv.s, hsv.v);
            RGBA out = inverted.toRGBA();
            out.a = this.toRGBA().a;
            return out;
        }

        private Color toExclusion(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();
            return new RGBA(
                    aC.r + bC.r - 2 * aC.r * bC.r,
                    aC.g + bC.g - 2 * aC.g * bC.g,
                    aC.b + bC.b - 2 * aC.b * bC.b,
                    aC.a + bC.a - 2 * aC.a * bC.a
            );
        }

        private Color toHardLight(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();
            double r = (bC.r < 0.5) ? (2 * aC.r * bC.r) : (1 - 2 * (1 - aC.r) * (1 - bC.r));
            double g = (bC.g < 0.5) ? (2 * aC.g * bC.g) : (1 - 2 * (1 - aC.g) * (1 - bC.g));
            double b = (bC.b < 0.5) ? (2 * aC.b * bC.b) : (1 - 2 * (1 - aC.b) * (1 - bC.b));
            double a = (bC.a < 0.5) ? (2 * aC.a * bC.a) : (1 - 2 * (1 - aC.a) * (1 - bC.a));
            return new RGBA(r, g, b, a);
        }

        private Color toSoftLight(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();
            double r = (bC.r < 0.5) ? (aC.r - (1 - 2 * bC.r) * aC.r * (1 - aC.r))
                    : (aC.r + (2 * bC.r - 1) * (Math.sqrt(aC.r) - aC.r));
            double g = (bC.g < 0.5) ? (aC.g - (1 - 2 * bC.g) * aC.g * (1 - aC.g))
                    : (aC.g + (2 * bC.g - 1) * (Math.sqrt(aC.g) - aC.g));
            double b = (bC.b < 0.5) ? (aC.b - (1 - 2 * bC.b) * aC.b * (1 - aC.b))
                    : (aC.b + (2 * bC.b - 1) * (Math.sqrt(aC.b) - aC.b));
            double a = (bC.a < 0.5) ? (aC.a - (1 - 2 * bC.a) * aC.a * (1 - aC.a))
                    : (aC.a + (2 * bC.a - 1) * (Math.sqrt(aC.a) - aC.a));
            return new RGBA(r, g, b, a);
        }

        private Color toColorBurn(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();
            double r = (bC.r == 0) ? 0 : 1 - Math.min(1, (1 - aC.r) / bC.r);
            double g = (bC.g == 0) ? 0 : 1 - Math.min(1, (1 - aC.g) / bC.g);
            double b = (bC.b == 0) ? 0 : 1 - Math.min(1, (1 - aC.b) / bC.b);
            double a = (bC.a == 0) ? 0 : 1 - Math.min(1, (1 - aC.a) / bC.a);
            return new RGBA(r, g, b, a);
        }

        private Color toColorDodge(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();
            double r = (bC.r == 1) ? 1 : Math.min(1, aC.r / (1 - bC.r));
            double g = (bC.g == 1) ? 1 : Math.min(1, aC.g / (1 - bC.g));
            double b = (bC.b == 1) ? 1 : Math.min(1, aC.b / (1 - bC.b));
            double a = (bC.a == 1) ? 1 : Math.min(1, aC.a / (1 - bC.a));
            return new RGBA(r, g, b, a);
        }

        private Color toGlow(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();
            double r = (aC.r == 1) ? 1 : Math.min(1, bC.r / (1 - aC.r));
            double g = (aC.g == 1) ? 1 : Math.min(1, bC.g / (1 - aC.g));
            double b = (aC.b == 1) ? 1 : Math.min(1, bC.b / (1 - aC.b));
            double a = (aC.a == 1) ? 1 : Math.min(1, bC.a / (1 - aC.a));
            return new RGBA(r, g, b, a);
        }

        private Color toReflect(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();
            double r = (bC.r == 0) ? 0 : Math.min(1, aC.r * aC.r / (1 - bC.r));
            double g = (bC.g == 0) ? 0 : Math.min(1, aC.g * aC.g / (1 - bC.g));
            double b = (bC.b == 0) ? 0 : Math.min(1, aC.b * aC.b / (1 - bC.b));
            double a = (bC.a == 0) ? 0 : Math.min(1, aC.a * aC.a / (1 - bC.a));
            return new RGBA(r, g, b, a);
        }

        private Color toXor(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();
            double r = (aC.r + bC.r) % 1.0;
            double g = (aC.g + bC.g) % 1.0;
            double b = (aC.b + bC.b) % 1.0;
            double a = (aC.a + bC.a) % 1.0;
            return new RGBA(r, g, b, a);
        }

        private Color toVividLight(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();

            double r = (bC.r < 0.5)
                    ? ((bC.r == 0) ? 0 : 1 - Math.min(1, (1 - aC.r) / (2 * bC.r)))
                    : ((bC.r == 1) ? 1 : Math.min(1, aC.r / (2 * (1 - bC.r))));
            double g = (bC.g < 0.5)
                    ? ((bC.g == 0) ? 0 : 1 - Math.min(1, (1 - aC.g) / (2 * bC.g)))
                    : ((bC.g == 1) ? 1 : Math.min(1, aC.g / (2 * (1 - bC.g))));
            double b = (bC.b < 0.5)
                    ? ((bC.b == 0) ? 0 : 1 - Math.min(1, (1 - aC.b) / (2 * bC.b)))
                    : ((bC.b == 1) ? 1 : Math.min(1, aC.b / (2 * (1 - bC.b))));
            double a = (bC.a < 0.5)
                    ? ((bC.a == 0) ? 0 : 1 - Math.min(1, (1 - aC.a) / (2 * bC.a)))
                    : ((bC.a == 1) ? 1 : Math.min(1, aC.a / (2 * (1 - bC.a))));

            return new RGBA(r, g, b, a);
        }

        private Color toLinearLight(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();

            double r = aC.r + 2 * bC.r - 1;
            double g = aC.g + 2 * bC.g - 1;
            double b = aC.b + 2 * bC.b - 1;
            double a = aC.a + 2 * bC.a - 1;

            r = Math.clamp(r, 0, 1);
            g = Math.clamp(g, 0, 1);
            b = Math.clamp(b, 0, 1);
            a = Math.clamp(a, 0, 1);

            return new RGBA(r, g, b, a);
        }

        private Color toPinLight(Color value) {
            RGBA aC = this.toRGBA();
            RGBA bC = value.toRGBA();

            double r = (bC.r < 0.5)
                    ? Math.min(aC.r, 2 * bC.r)
                    : Math.max(aC.r, 2 * bC.r - 1);
            double g = (bC.g < 0.5)
                    ? Math.min(aC.g, 2 * bC.g)
                    : Math.max(aC.g, 2 * bC.g - 1);
            double b = (bC.b < 0.5)
                    ? Math.min(aC.b, 2 * bC.b)
                    : Math.max(aC.b, 2 * bC.b - 1);
            double a = (bC.a < 0.5)
                    ? Math.min(aC.a, 2 * bC.a)
                    : Math.max(aC.a, 2 * bC.a - 1);

            r = Math.clamp(r, 0, 1);
            g = Math.clamp(g, 0, 1);
            b = Math.clamp(b, 0, 1);
            a = Math.clamp(a, 0, 1);

            return new RGBA(r, g, b, a);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Color color) return equal(color);
            return false;
        }
    }

    public static class CMYK extends Color {
        public double c;
        public double m;
        public double y;
        public double k;

        public CMYK(double c, double m, double y, double k) {
            this.c = c;
            this.m = m;
            this.y = y;
            this.k = k;
        }

        @Override
        public RGB toRGB() {
            double R = 1 - Math.min(1, c * (1 - k) + k);
            double G = 1 - Math.min(1, m * (1 - k) + k);
            double B = 1 - Math.min(1, y * (1 - k) + k);
            return new RGB(R, G, B);
        }

        @Override
        public RGBA toRGBA() {
            RGB rgb = toRGB();
            return new RGBA(rgb.r, rgb.g, rgb.b, 1.0);
        }

        @Override
        public String toShortString() {
            return String.format("c%.2f, m%.2f, y%.2f, k%.2f", c, m, y, k);
        }

        @Override
        public String toShortString255() {
            return String.format("c%d%%, m%d%%, y%d%%, k%d%%", (int)(c*100), (int)(m*100), (int)(y*100), (int)(k*100));
        }

        @Override
        public String toString() {
            return String.format("Cyan %.3f, Magenta %.3f, Yellow %.3f, Black %.3f", c, m, y, k);
        }

        @Override
        public String toString255() {
            return String.format("Cyan %d%%, Magenta %d%%, Yellow %d%%, Black %d%%", (int)(c*100), (int)(m*100), (int)(y*100), (int)(k*100));
        }

        @Override
        public int toHex() {
            return toRGB().toHex();
        }

        @Override
        public String toHexString() {
            return toRGB().toHexString();
        }

        @Override
        public Vector3d toVec3() {
            return toRGB().toVec3();
        }

        @Override
        public Vector4d toVec4() {
            return toRGBA().toVec4();
        }

        @Override
        public RYB toRYB() {
            return toRGB().toRYB();
        }

        @Override
        public RYBA toRYBA() {
            return toRGB().toRYBA();
        }

        @Override
        public HSV toHSV() {
            return toRGB().toHSV();
        }

        @Override
        public HSVA toHSVA() {
            return toRGB().toHSVA();
        }

        @Override
        public HSL toHSL() {
            return toRGB().toHSL();
        }

        @Override
        public HSLA toHSLA() {
            return toRGB().toHSLA();
        }

        @Override
        public HWB toHWB() {
            return toRGB().toHWB();
        }

        @Override
        public HWBA toHWBA() {
            return toRGB().toHWBA();
        }

        @Override
        public CMYK toCMYK() {
            return this;
        }

        @Override
        public CMYKA toCMYKA() {
            return new CMYKA(c, m, y, k, 1.0);
        }

        @Override
        public boolean equal(Color other) {
            if (other == null) return false;
            RGBA a = this.toRGBA();
            RGBA b = other.toRGBA();
            return Math.abs(a.r - b.r) < 1e-6 && Math.abs(a.g - b.g) < 1e-6 && Math.abs(a.b - b.b) < 1e-6 && Math.abs(a.a - b.a) < 1e-6;
        }
    }

    public static class CMYKA extends CMYK {
        public double a;

        public CMYKA(double c, double m, double y, double k, double a) {
            super(c, m, y, k);
            this.a = a;
        }
    }

    public static class HWB extends Color {
        public double h;
        public double w;
        public double b;

        public HWB(double h, double w, double b) {
            this.h = h;
            this.w = w;
            this.b = b;
        }

        @Override
        public RGB toRGB() {
            HSV base = new HSV(h, 1.0, 1.0);
            RGB rgb = base.toRGB();

            double sum = w + b;
            if (sum > 1.0) {
                w /= sum;
                b /= sum;
            }

            double R = rgb.r * (1 - w - b) + w;
            double G = rgb.g * (1 - w - b) + w;
            double B = rgb.b * (1 - w - b) + w;

            return new RGB(R, G, B);
        }

        @Override
        public RGBA toRGBA() {
            RGB rgb = toRGB();
            return new RGBA(rgb.r, rgb.g, rgb.b, 1.0);
        }

        @Override
        public String toShortString() {
            return String.format("h%.1f, w%.2f, b%.2f", h, w, b);
        }

        @Override
        public String toShortString255() {
            return String.format("h%.1f°, w%d%%, b%d%%", h, (int)(w*100), (int)(b*100));
        }

        @Override
        public String toString() {
            return String.format("Hue %.1f°, Whiteness %.3f, Blackness %.3f", h, w, b);
        }

        @Override
        public String toString255() {
            return String.format("Hue %.1f°, Whiteness %d%%, Blackness %d%%", h, (int)(w*100), (int)(b*100));
        }

        @Override
        public int toHex() {
            return toRGB().toHex();
        }

        @Override
        public String toHexString() {
            return toRGB().toHexString();
        }

        @Override
        public Vector3d toVec3() {
            return toRGB().toVec3();
        }

        @Override
        public Vector4d toVec4() {
            return toRGBA().toVec4();
        }

        @Override
        public RYB toRYB() {
            return toRGB().toRYB();
        }

        @Override
        public RYBA toRYBA() {
            return toRGB().toRYBA();
        }

        @Override
        public HSV toHSV() {
            return toRGB().toHSV();
        }

        @Override
        public HSVA toHSVA() {
            return toRGB().toHSVA();
        }

        @Override
        public HSL toHSL() {
            return toRGB().toHSL();
        }

        @Override
        public HSLA toHSLA() {
            return toRGB().toHSLA();
        }

        @Override
        public HWB toHWB() {
            return this;
        }

        @Override
        public HWBA toHWBA() {
            return new HWBA(h, w, b, 1.0);
        }

        @Override
        public CMYK toCMYK() {
            return toRGB().toCMYK();
        }

        @Override
        public CMYKA toCMYKA() {
            return toRGB().toCMYKA();
        }

        @Override
        public boolean equal(Color other) {
            if (other == null) return false;
            RGBA a = this.toRGBA();
            RGBA b = other.toRGBA();
            return Math.abs(a.r - b.r) < 1e-6 && Math.abs(a.g - b.g) < 1e-6 && Math.abs(a.b - b.b) < 1e-6 && Math.abs(a.a - b.a) < 1e-6;
        }
    }

    public static class HWBA extends HWB {
        public double a;

        public HWBA(double h, double w, double b, double a) {
            super(h, w, b);
            this.a = a;
        }
    }

    public static class HSL extends Color {
        public double h;
        public double s;
        public double l;

        public HSL(double h, double s, double l) {
            this.h = h;
            this.s = s;
            this.l = l;
        }

        @Override
        public RGB toRGB() {
            double c = (1 - Math.abs(2 * l - 1)) * s;
            double x = c * (1 - Math.abs((h / 60.0) % 2 - 1));
            double m = l - c / 2;

            double r1=0, g1=0, b1=0;
            if (0 <= h && h < 60) { r1 = c; g1 = x; b1 = 0; }
            else if (60 <= h && h < 120) { r1 = x; g1 = c; b1 = 0; }
            else if (120 <= h && h < 180) { r1 = 0; g1 = c; b1 = x; }
            else if (180 <= h && h < 240) { r1 = 0; g1 = x; b1 = c; }
            else if (240 <= h && h < 300) { r1 = x; g1 = 0; b1 = c; }
            else if (300 <= h && h < 360) { r1 = c; g1 = 0; b1 = x; }

            return new RGB(r1 + m, g1 + m, b1 + m);
        }

        @Override
        public RGBA toRGBA() {
            RGB rgb = toRGB();
            return new RGBA(rgb.r, rgb.g, rgb.b, 1.0);
        }

        @Override
        public String toShortString() {
            return String.format("h%.1f, s%.2f, l%.2f", h, s, l);
        }

        @Override
        public String toShortString255() {
            return String.format("h%.1f°, s%d%%, l%d%%", h, (int)(s*100), (int)(l*100));
        }

        @Override
        public String toString() {
            return String.format("Hue %.1f°, Saturation %.3f, Lightness %.3f", h, s, l);
        }

        @Override
        public String toString255() {
            return String.format("Hue %.1f°, Saturation %d%%, Lightness %d%%", h, (int)(s*100), (int)(l*100));
        }

        @Override
        public int toHex() {
            return toRGB().toHex();
        }

        @Override
        public String toHexString() {
            return toRGB().toHexString();
        }

        @Override
        public Vector3d toVec3() {
            return toRGB().toVec3();
        }

        @Override
        public Vector4d toVec4() {
            return toRGBA().toVec4();
        }

        @Override
        public RYB toRYB() {
            return toRGB().toRYB();
        }

        @Override
        public RYBA toRYBA() {
            return toRGB().toRYBA();
        }

        @Override
        public HSV toHSV() {
            return toRGB().toHSV();
        }

        @Override
        public HSVA toHSVA() {
            return toRGB().toHSVA();
        }

        @Override
        public HSL toHSL() {
            return this;
        }

        @Override
        public HSLA toHSLA() {
            return new HSLA(h, s, l, 1.0);
        }

        @Override
        public HWB toHWB() {
            return toRGB().toHWB();
        }

        @Override
        public HWBA toHWBA() {
            return toRGB().toHWBA();
        }

        @Override
        public CMYK toCMYK() {
            return toRGB().toCMYK();
        }

        @Override
        public CMYKA toCMYKA() {
            return toRGB().toCMYKA();
        }

        @Override
        public boolean equal(Color other) {
            if (other == null) return false;
            RGBA a = this.toRGBA();
            RGBA b = other.toRGBA();
            return Math.abs(a.r - b.r) < 1e-6 && Math.abs(a.g - b.g) < 1e-6 && Math.abs(a.b - b.b) < 1e-6 && Math.abs(a.a - b.a) < 1e-6;
        }
    }

    public static class HSLA extends HSL {
        public double a;

        public HSLA(double h, double s, double l, double a) {
            super(h, s, l);
            this.a = a;
        }
    }

    public static class HSV extends Color {
        public double h;
        public double s;
        public double v;

        public HSV(double h, double s, double v) {
            this.h = h;
            this.s = s;
            this.v = v;
        }

        @Override
        public RGB toRGB() {
            double c = v * s;
            double x = c * (1 - Math.abs((h / 60.0) % 2 - 1));
            double m = v - c;

            double r1=0, g1=0, b1=0;
            if (0 <= h && h < 60) { r1 = c; g1 = x; b1 = 0; }
            else if (60 <= h && h < 120) { r1 = x; g1 = c; b1 = 0; }
            else if (120 <= h && h < 180) { r1 = 0; g1 = c; b1 = x; }
            else if (180 <= h && h < 240) { r1 = 0; g1 = x; b1 = c; }
            else if (240 <= h && h < 300) { r1 = x; g1 = 0; b1 = c; }
            else if (300 <= h && h < 360) { r1 = c; g1 = 0; b1 = x; }

            return new RGB(r1 + m, g1 + m, b1 + m);
        }

        @Override
        public RGBA toRGBA() {
            RGB rgb = toRGB();
            return new RGBA(rgb.r, rgb.g, rgb.b, 1.0);
        }

        @Override
        public String toShortString() {
            return String.format("h%.1f, s%.2f, v%.2f", h, s, v);
        }

        @Override
        public String toShortString255() {
            return String.format("h%.1f°, s%d%%, v%d%%", h, (int)(s*100), (int)(v*100));
        }

        @Override
        public String toString() {
            return String.format("Hue %.1f°, Saturation %.3f, Value %.3f", h, s, v);
        }

        @Override
        public String toString255() {
            return String.format("Hue %.1f°, Saturation %d%%, Value %d%%", h, (int)(s*100), (int)(v*100));
        }

        @Override
        public int toHex() {
            return toRGB().toHex();
        }

        @Override
        public String toHexString() {
            return toRGB().toHexString();
        }

        @Override
        public Vector3d toVec3() {
            return toRGB().toVec3();
        }

        @Override
        public Vector4d toVec4() {
            return toRGBA().toVec4();
        }

        @Override
        public RYB toRYB() {
            return toRGB().toRYB();
        }

        @Override
        public RYBA toRYBA() {
            return toRGB().toRYBA();
        }

        @Override
        public HSV toHSV() {
            return this;
        }

        @Override
        public HSVA toHSVA() {
            return new HSVA(h, s, v, 1.0);
        }

        @Override
        public HSL toHSL() {
            return toRGB().toHSL();
        }

        @Override
        public HSLA toHSLA() {
            return toRGB().toHSLA();
        }

        @Override
        public HWB toHWB() {
            return toRGB().toHWB();
        }

        @Override
        public HWBA toHWBA() {
            return toRGB().toHWBA();
        }

        @Override
        public CMYK toCMYK() {
            return toRGB().toCMYK();
        }

        @Override
        public CMYKA toCMYKA() {
            return toRGB().toCMYKA();
        }

        @Override
        public boolean equal(Color other) {
            if (other == null) return false;
            RGBA a = this.toRGBA();
            RGBA b = other.toRGBA();
            return Math.abs(a.r - b.r) < 1e-6 && Math.abs(a.g - b.g) < 1e-6 && Math.abs(a.b - b.b) < 1e-6 && Math.abs(a.a - b.a) < 1e-6;
        }
    }

    public static class HSVA extends HSV {
        public double a;

        public HSVA(double h, double s, double v, double a) {
            super(h, s, v);
            this.a = a;
        }
    }

    public static class RYB extends Color {
        public double r;
        public double y;
        public double b;

        public RYB(double r, double y, double b) {
            this.r = r;
            this.y = y;
            this.b = b;
        }

        @Override
        public RGB toRGB() {
            double R = r + y * (1 - r) - b * r * 0.5;
            double G = y * (1 - b) + b * (1 - y) * 0.5;
            double B = b + r * (1 - b) - y * b * 0.5;
            return new RGB(R, G, B);
        }

        @Override
        public RGBA toRGBA() {
            RGB rgb = toRGB();
            return new RGBA(rgb.r, rgb.g, rgb.b, 1.0);
        }

        @Override
        public String toShortString() {
            return String.format("r%.2f, y%.2f, b%.2f", r, y, b);
        }

        @Override
        public String toShortString255() {
            return String.format("r%d, y%d, b%d", (int)(r*255), (int)(y*255), (int)(b*255));
        }

        @Override
        public String toString() {
            return String.format("Red %.3f, Yellow %.3f, Blue %.3f", r, y, b);
        }

        @Override
        public String toString255() {
            return String.format("Red %d, Yellow %d, Blue %d", (int)(r*255), (int)(y*255), (int)(b*255));
        }

        @Override
        public int toHex() {
            return toRGB().toHex();
        }

        @Override
        public String toHexString() {
            return toRGB().toHexString();
        }

        @Override
        public Vector3d toVec3() {
            return toRGB().toVec3();
        }

        @Override
        public Vector4d toVec4() {
            return toRGBA().toVec4();
        }

        @Override
        public RYB toRYB() {
            return this;
        }

        @Override
        public RYBA toRYBA() {
            return new RYBA(r, y, b, 1.0);
        }

        @Override
        public HSV toHSV() {
            return toRGB().toHSV();
        }

        @Override
        public HSVA toHSVA() {
            return toRGB().toHSVA();
        }

        @Override
        public HSL toHSL() {
            return toRGB().toHSL();
        }

        @Override
        public HSLA toHSLA() {
            return toRGB().toHSLA();
        }

        @Override
        public HWB toHWB() {
            return toRGB().toHWB();
        }

        @Override
        public HWBA toHWBA() {
            return toRGB().toHWBA();
        }

        @Override
        public CMYK toCMYK() {
            return toRGB().toCMYK();
        }

        @Override
        public CMYKA toCMYKA() {
            return toRGB().toCMYKA();
        }

        @Override
        public boolean equal(Color other) {
            if (other == null) return false;
            RGBA a = this.toRGBA();
            RGBA b = other.toRGBA();
            return Math.abs(a.r - b.r) < 1e-6 && Math.abs(a.g - b.g) < 1e-6 && Math.abs(a.b - b.b) < 1e-6 && Math.abs(a.a - b.a) < 1e-6;
        }
    }

    public static class RYBA extends RYB {
        public double a;

        public RYBA(double r, double y, double b, double a) {
            super(r, y, b);
            this.a = a;
        }


    }

    public static class RGB extends Color {
        public double r;
        public double g;
        public double b;

        public RGB(double r, double g, double b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        @Override
        public int toHex() {
            int r = (int) Math.round(this.r * 255), g = (int) Math.round(this.g * 255), b = (int) Math.round(this.b * 255);
            return (r << 16) | (g << 8) | b;
        }

        @Override
        public String toHexString() {
            return String.format("#%02X%02X%02X", (int)(r*255), (int)(g*255), (int)(b*255));
        }

        @Override
        public Vector3d toVec3() {
            return new Vector3d(r, g, b);
        }

        @Override
        public Vector4d toVec4() {
            return new Vector4d(r, g, b, 1.0);
        }

        @Override
        public RGB toRGB() {
            return this;
        }

        @Override
        public RGBA toRGBA() {
            return new RGBA(r, g, b, 1.0);
        }

        @Override
        public String toShortString() {
            return String.format("r%.2f, g%.2f, b%.2f", r, g, b);
        }

        @Override
        public String toShortString255() {
            return String.format("r%d, g%d, b%d", (int)(r*255), (int)(g*255), (int)(b*255));
        }

        @Override
        public String toString() {
            return String.format("Red %.3f, Green %.3f, Blue %.3f", r, g, b);
        }

        @Override
        public String toString255() {
            return String.format("Red %d, Green %d, Blue %d", (int)(r*255), (int)(g*255), (int)(b*255));
        }

        @Override
        public boolean equal(Color other) {
            if (other == null) return false;
            RGBA a = this.toRGBA();
            RGBA b = other.toRGBA();
            return Math.abs(a.r - b.r) < 1e-6 && Math.abs(a.g - b.g) < 1e-6 && Math.abs(a.b - b.b) < 1e-6 && Math.abs(a.a - b.a) < 1e-6;
        }

        @Override
        public RYB toRYB() {
            double Y = Math.min(r, g);
            r -= Y;
            g -= Y;
            double B = Math.min(g, b);
            g -= B;
            b -= B;
            double R = r;
            Y += g;
            B += b;
            return new RYB(R, Y, B);
        }

        @Override
        public RYBA toRYBA() {
            var c = toRYB();
            return new RYBA(c.r, c.y, c.b, 1.0);
        }

        @Override
        public HSV toHSV() {
            double max = Math.max(r, Math.max(g, b));
            double min = Math.min(r, Math.min(g, b));
            double delta = max - min;

            double h;
            if (delta == 0) h = 0;
            else if (max == r) h = 60 * (((g - b) / delta) % 6);
            else if (max == g) h = 60 * (((b - r) / delta) + 2);
            else h = 60 * (((r - g) / delta) + 4);
            if (h < 0) h += 360;

            double s = (max == 0) ? 0 : delta / max;
            return new HSV(h, s, max);
        }

        @Override
        public HSVA toHSVA() {
            var c = toHSV();
            return new HSVA(c.h, c.s, c.v, 1.0);
        }

        @Override
        public HSL toHSL() {
            double max = Math.max(r, Math.max(g, b));
            double min = Math.min(r, Math.min(g, b));
            double delta = max - min;

            double h;
            if (delta == 0) h = 0;
            else if (max == r) h = 60 * (((g - b) / delta) % 6);
            else if (max == g) h = 60 * (((b - r) / delta) + 2);
            else h = 60 * (((r - g) / delta) + 4);
            if (h < 0) h += 360;

            double l = (max + min) / 2;
            double s = (delta == 0) ? 0 : delta / (1 - Math.abs(2 * l - 1));

            return new HSL(h, s, l);
        }

        @Override
        public HSLA toHSLA() {
            var c = toHSL();
            return new HSLA(c.h, c.s, c.l, 1.0);
        }

        @Override
        public HWB toHWB() {
            HSV hsv = toHSV();
            double w = Math.min(r, Math.min(g, b));
            double bl = 1 - Math.max(r, Math.max(g, b));
            return new HWB(hsv.h, w, bl);
        }

        @Override
        public HWBA toHWBA() {
            var c = toHWB();
            return new HWBA(c.h, c.w, c.b, 1.0);
        }

        @Override
        public CMYK toCMYK() {
            double k = 1 - Math.max(r, Math.max(g, b));
            if (k >= 1.0) return new CMYK(0, 0, 0, 1);
            double c = (1 - r - k) / (1 - k);
            double m = (1 - g - k) / (1 - k);
            double y = (1 - b - k) / (1 - k);
            return new CMYK(c, m, y, k);
        }

        @Override
        public CMYKA toCMYKA() {
            var c = toCMYK();
            return new CMYKA(c.c, c.m, c.y, c.k, 1.0);
        }
    }

    public static class RGBA extends RGB {
        public double a;

        public RGBA(double r, double g, double b, double a) {
            super(r, g, b);
            this.a = a;
        }

        @Override
        public int toHex() {
            int rI = (int) Math.round(r * 255), gI = (int) Math.round(g * 255), bI = (int) Math.round(b * 255), aI = (int) Math.round(a * 255);
            return (aI << 24) | (rI << 16) | (gI << 8) | bI;
        }

        @Override
        public String toHexString() {
            return String.format("#%02X%02X%02X%02X", (int)(a*255), (int)(r*255), (int)(g*255), (int)(b*255));
        }

        @Override
        public Vector4d toVec4() {
            return new Vector4d(r, g, b, a);
        }

        @Override
        public RGB toRGB() {
            return new RGB(r, g, b);
        }

        @Override
        public RGBA toRGBA() {
            return this;
        }

        @Override
        public RYBA toRYBA() {
            RYB ryb = super.toRYB();
            return new RYBA(ryb.r, ryb.y, ryb.b, a);
        }

        @Override
        public HSVA toHSVA() {
            HSV hsv = super.toHSV();
            return new HSVA(hsv.h, hsv.s, hsv.v, a);
        }

        @Override
        public HSLA toHSLA() {
            HSL hsl = super.toHSL();
            return new HSLA(hsl.h, hsl.s, hsl.l, a);
        }

        @Override
        public HWBA toHWBA() {
            HWB hwb = super.toHWB();
            return new HWBA(hwb.h, hwb.w, hwb.b, a);
        }

        @Override
        public CMYKA toCMYKA() {
            CMYK cmyk = super.toCMYK();
            return new CMYKA(cmyk.c, cmyk.m, cmyk.y, cmyk.k, a);
        }

        @Override
        public String toShortString() {
            return String.format("r%.2f, g%.2f, b%.2f, a%.2f", r, g, b, a);
        }

        @Override
        public String toShortString255() {
            return String.format("r%d, g%d, b%d, a%d", (int)(r*255), (int)(g*255), (int)(b*255), (int)(a*255));
        }

        @Override
        public String toString() {
            return String.format("Red %.3f, Green %.3f, Blue %.3f, Alpha %.3f", r, g, b, a);
        }

        @Override
        public String toString255() {
            return String.format("Red %d, Green %d, Blue %d, Alpha %d", (int)(r*255), (int)(g*255), (int)(b*255), (int)(a*255));
        }

        @Override
        public boolean equal(Color other) {
            if (other == null) return false;
            RGBA aC = this.toRGBA();
            RGBA bC = other.toRGBA();
            return Math.abs(aC.r - bC.r) < 1e-6 && Math.abs(aC.g - bC.g) < 1e-6 && Math.abs(aC.b - bC.b) < 1e-6 && Math.abs(aC.a - bC.a) < 1e-6;
        }
    }

    public static final class ColorRenderTypes {
        private static final Map<ColorBlendType, RenderType> TYPES = new EnumMap<>(ColorBlendType.class);
        public static void init() {
            TYPES.put(ColorBlendType.BLEND, rt("blend", TRANSLUCENT_TRANSPARENCY));

            putCustom(ColorBlendType.ADD, () -> RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE));
            putCustom(ColorBlendType.MULTIPLY, () -> RenderSystem.blendFuncSeparate(DST_COLOR, GlStateManager.DestFactor.ZERO, GlStateManager.SourceFactor.ONE, ONE_MINUS_SRC_ALPHA));
            putCustom(ColorBlendType.SCREEN,    () -> RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, ONE_MINUS_SRC_ALPHA));

            putCustom(ColorBlendType.GLOW, () -> {
                RenderSystem.blendEquation(GL_FUNC_ADD);
                RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            }, () -> RenderSystem.blendEquation(GL_FUNC_ADD));

            putCustom(ColorBlendType.SUBTRACT, () -> {
                RenderSystem.blendEquation(GL_FUNC_REVERSE_SUBTRACT);
                RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE,
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }, () -> RenderSystem.blendEquation(GL_FUNC_ADD));

            putCustom(ColorBlendType.DARKEN, () -> {
                RenderSystem.blendEquation(GL_MIN);
                RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE,
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }, () -> RenderSystem.blendEquation(GL_FUNC_ADD));

            putCustom(ColorBlendType.LIGHTEN, () -> {
                RenderSystem.blendEquation(GL_MAX);
                RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE,
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }, () -> RenderSystem.blendEquation(GL_FUNC_ADD));

            putCustom(ColorBlendType.AVERAGE, () -> {
                RenderSystem.blendEquation(GL_FUNC_ADD);
                glBlendColor(0f, 0f, 0f, 0.5f);
                RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA,
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }, () -> {
                glBlendColor(0f, 0f, 0f, 0f);
                RenderSystem.blendEquation(GL_FUNC_ADD);
            });

            putCustom(ColorBlendType.XOR, () -> {
                RenderSystem.blendEquation(GL_FUNC_ADD);
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            });
            putCustom(ColorBlendType.INVERT, () -> {
                RenderSystem.blendEquation(GL_FUNC_ADD);
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ZERO, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            });
        }

        public static RenderType get(ColorBlendType type) {
            return TYPES.getOrDefault(type, TYPES.get(ColorBlendType.BLEND));
        }

        private static void putCustom(ColorBlendType t, Runnable blendFunc) {
            TYPES.put(t, rt("cm_" + t.name().toLowerCase(Locale.ROOT), rt(t.name().toLowerCase(Locale.ROOT) + "_transparency", blendFunc)));
        }

        private static void putCustom(ColorBlendType t, Runnable blendFuncPre, Runnable blendFuncPost) {
            TYPES.put(t, rt("cm_" + t.name().toLowerCase(Locale.ROOT), rt(t.name().toLowerCase(Locale.ROOT) + "_transparency", blendFuncPre, blendFuncPost)));
        }

        private static TransparencyStateShard rt(String name, Runnable blendFuncPre, Runnable blendFuncPost) {
            return new TransparencyStateShard(name, () -> {
                RenderSystem.enableBlend();
                blendFuncPre.run();
            }, () -> {
                blendFuncPost.run();
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });
        }

        private static TransparencyStateShard rt(String name, Runnable blendFunc) {
            return new TransparencyStateShard(name, () -> {
                RenderSystem.enableBlend();
                blendFunc.run();
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });
        }

        private static RenderType rt(String name, TransparencyStateShard transparency) {
            return RenderType.create("color_" + name,
                    DefaultVertexFormat.BLOCK,
                    VertexFormat.Mode.QUADS,
                    4194304,
                    true,
                    true,
                    translucentState(transparency)
            );
        }

        private static RenderType.CompositeState translucentState(TransparencyStateShard transparency) {
            return RenderType.CompositeState.builder()
                    .setLightmapState(LIGHTMAP)
                    .setShaderState(RenderStateShard.RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(BLOCK_SHEET_MIPPED)
                    .setTransparencyState(transparency)
                    .setOutputState(TRANSLUCENT_TARGET)
                    .setCullState(NO_CULL)
                    .createCompositeState(true);
        }
    }
}
