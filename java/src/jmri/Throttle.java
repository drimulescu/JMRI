package jmri;

import java.util.Vector;

/**
 * A Throttle object can be manipulated to change the speed, direction and
 * functions of a single locomotive.
 * <P>
 * A Throttle implementation provides the actual control mechanism. These are
 * obtained via a {@link ThrottleManager}.
 * <P>
 * With some control systems, there are only a limited number of Throttle's
 * available.
 * <p>
 * On DCC systems, Throttles are often actually {@link DccThrottle} objects,
 * which have some additional DCC-specific capabilities.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public abstract class Throttle {

    /**
     * Constants to represent the functions F0 through F28.
     */
    public static final String F0 = "F0"; // NOI18N
    public static final String F1 = "F1"; // NOI18N
    public static final String F2 = "F2"; // NOI18N
    public static final String F3 = "F3"; // NOI18N
    public static final String F4 = "F4"; // NOI18N
    public static final String F5 = "F5"; // NOI18N
    public static final String F6 = "F6"; // NOI18N
    public static final String F7 = "F7"; // NOI18N
    public static final String F8 = "F8"; // NOI18N
    public static final String F9 = "F9"; // NOI18N
    public static final String F10 = "F10"; // NOI18N
    public static final String F11 = "F11"; // NOI18N
    public static final String F12 = "F12"; // NOI18N
    public static final String F13 = "F13"; // NOI18N
    public static final String F14 = "F14"; // NOI18N
    public static final String F15 = "F15"; // NOI18N
    public static final String F16 = "F16"; // NOI18N
    public static final String F17 = "F17"; // NOI18N
    public static final String F18 = "F18"; // NOI18N
    public static final String F19 = "F19"; // NOI18N
    public static final String F20 = "F20"; // NOI18N
    public static final String F21 = "F21"; // NOI18N
    public static final String F22 = "F22"; // NOI18N
    public static final String F23 = "F23"; // NOI18N
    public static final String F24 = "F24"; // NOI18N
    public static final String F25 = "F25"; // NOI18N
    public static final String F26 = "F26"; // NOI18N
    public static final String F27 = "F27"; // NOI18N
    public static final String F28 = "F28"; // NOI18N
    /**
     * Constants to represent the functions F0 through F28.
     */
    public static final String F0Momentary = "F0Momentary"; // NOI18N
    public static final String F1Momentary = "F1Momentary"; // NOI18N
    public static final String F2Momentary = "F2Momentary"; // NOI18N
    public static final String F3Momentary = "F3Momentary"; // NOI18N
    public static final String F4Momentary = "F4Momentary"; // NOI18N
    public static final String F5Momentary = "F5Momentary"; // NOI18N
    public static final String F6Momentary = "F6Momentary"; // NOI18N
    public static final String F7Momentary = "F7Momentary"; // NOI18N
    public static final String F8Momentary = "F8Momentary"; // NOI18N
    public static final String F9Momentary = "F9Momentary"; // NOI18N
    public static final String F10Momentary = "F10Momentary"; // NOI18N
    public static final String F11Momentary = "F11Momentary"; // NOI18N
    public static final String F12Momentary = "F12Momentary"; // NOI18N
    public static final String F13Momentary = "F13Momentary"; // NOI18N
    public static final String F14Momentary = "F14Momentary"; // NOI18N
    public static final String F15Momentary = "F15Momentary"; // NOI18N
    public static final String F16Momentary = "F16Momentary"; // NOI18N
    public static final String F17Momentary = "F17Momentary"; // NOI18N
    public static final String F18Momentary = "F18Momentary"; // NOI18N
    public static final String F19Momentary = "F19Momentary"; // NOI18N
    public static final String F20Momentary = "F20Momentary"; // NOI18N
    public static final String F21Momentary = "F21Momentary"; // NOI18N
    public static final String F22Momentary = "F22Momentary"; // NOI18N
    public static final String F23Momentary = "F23Momentary"; // NOI18N
    public static final String F24Momentary = "F24Momentary"; // NOI18N
    public static final String F25Momentary = "F25Momentary"; // NOI18N
    public static final String F26Momentary = "F26Momentary"; // NOI18N
    public static final String F27Momentary = "F27Momentary"; // NOI18N
    public static final String F28Momentary = "F28Momentary"; // NOI18N

    /**
     * Speed - expressed as a value {@literal 0.0 -> 1.0.} Negative means
     * emergency stop. This is an bound property.
     *
     * @return the speed as a percentage of maximum possible speed
     */
    public abstract float getSpeedSetting();

    public abstract void setSpeedSetting(float speed);

    /**
     * direction This is an bound property.
     *
     * @return true if forward, false if reverse or undefined
     */
    public abstract boolean getIsForward();

    public abstract void setIsForward(boolean forward);

    // functions - note that we use the naming for DCC, though that's not the implication;
    // see also DccThrottle interface
    public abstract boolean getF(int function);
    
    public abstract void setF(int function, boolean value);
    
    public final boolean getF0() { return getF(0); }

    public final void setF0(boolean f0) { setF(0, f0); }

    public final boolean getF1() { return getF(1); }

    public final void setF1(boolean f1) { setF(1, f1); }

    public final boolean getF2() { return getF(2); }

    public final void setF2(boolean f2) { setF(2, f2); }

    public final boolean getF3() { return getF(3); }

    public final void setF3(boolean f3) { setF(3, f3); }

    public final boolean getF4() { return getF(4); }

    public final void setF4(boolean f4) { setF(4, f4); }

    public final boolean getF5() { return getF(5); }

    public final void setF5(boolean f5) { setF(5, f5); }

    public final boolean getF6() { return getF(6); }

    public final void setF6(boolean f6) { setF(6, f6); }

    public final boolean getF7() { return getF(7); }

    public final void setF7(boolean f7) { setF(7, f7); }

    public final boolean getF8() { return getF(8); }

    public final void setF8(boolean f8) { setF(8, f8); }

    public final boolean getF9() { return getF(9); }

    public final void setF9(boolean f9) { setF(9, f9); }

    public final boolean getF10() { return getF(10); }

    public final void setF10(boolean f10) { setF(10, f10); }

    public final boolean getF11() { return getF(11); }

    public final void setF11(boolean f11) { setF(11, f11); }

    public final boolean getF12() { return getF(12); }

    public final void setF12(boolean f12) { setF(12, f12); }

    public final boolean getF13() { return getF(13); }

    public final void setF13(boolean f13) { setF(13, f13); }

    public final boolean getF14() { return getF(14); }

    public final void setF14(boolean f14) { setF(14, f14); }

    public final boolean getF15() { return getF(15); }

    public final void setF15(boolean f15) { setF(15, f15); }

    public final boolean getF16() { return getF(16); }

    public final void setF16(boolean f16) { setF(16, f16); }

    public final boolean getF17() { return getF(17); }

    public final void setF17(boolean f17) { setF(17, f17); }

    public final boolean getF18() { return getF(18); }

    public final void setF18(boolean f18) { setF(18, f18); }

    public final boolean getF19() { return getF(19); }

    public final void setF19(boolean f19) { setF(19, f19); }

    public final boolean getF20() { return getF(20); }

    public final void setF20(boolean f20) { setF(20, f20); }

    public final boolean getF21() { return getF(21); }

    public final void setF21(boolean f21) { setF(21, f21); }

    public final boolean getF22() { return getF(22); }

    public final void setF22(boolean f22) { setF(22, f22); }

    public final boolean getF23() { return getF(23); }

    public final void setF23(boolean f23) { setF(23, f23); }

    public final boolean getF24() { return getF(24); }

    public final void setF24(boolean f24) { setF(24, f24); }

    public final boolean getF25() { return getF(25); }

    public final void setF25(boolean f25) { setF(25, f25); }

    public final boolean getF26() { return getF(26); }

    public final void setF26(boolean f26) { setF(26, f26); }

    public final boolean getF27() { return getF(27); }

    public final void setF27(boolean f27) { setF(27, f27); }

    public final boolean getF28() { return getF(28); }

    public final void setF28(boolean f28) { setF(28, f28); }

    // functions momentary status - note that we use the naming for DCC, 
    // though that's not the implication;
    // see also DccThrottle interface
    public abstract boolean getFMomentary(int function);

    public abstract void setFMomentary(int function, boolean value);

    public final boolean getF0Momentary() { return getFMomentary(0); }

    public final void setF0Momentary(boolean f0Momentary) { setFMomentary(0, f0Momentary); }

    public final boolean getF1Momentary() { return getFMomentary(1); }

    public final void setF1Momentary(boolean f1Momentary) { setFMomentary(1, f1Momentary); }

    public final boolean getF2Momentary() { return getFMomentary(2); }

    public final void setF2Momentary(boolean f2Momentary) { setFMomentary(2, f2Momentary); }

    public final boolean getF3Momentary() { return getFMomentary(3); }

    public final void setF3Momentary(boolean f3Momentary) { setFMomentary(3, f3Momentary); }

    public final boolean getF4Momentary() { return getFMomentary(4); }

    public final void setF4Momentary(boolean f4Momentary) { setFMomentary(4, f4Momentary); }

    public final boolean getF5Momentary() { return getFMomentary(5); }

    public final void setF5Momentary(boolean f5Momentary) { setFMomentary(5, f5Momentary); }

    public final boolean getF6Momentary() { return getFMomentary(6); }

    public final void setF6Momentary(boolean f6Momentary) { setFMomentary(6, f6Momentary); }

    public final boolean getF7Momentary() { return getFMomentary(7); }

    public final void setF7Momentary(boolean f7Momentary) { setFMomentary(7, f7Momentary); }

    public final boolean getF8Momentary() { return getFMomentary(8); }

    public final void setF8Momentary(boolean f8Momentary) { setFMomentary(8, f8Momentary); }

    public final boolean getF9Momentary() { return getFMomentary(9); }

    public final void setF9Momentary(boolean f9Momentary) { setFMomentary(9, f9Momentary); }

    public final boolean getF10Momentary() { return getFMomentary(10); }

    public final void setF10Momentary(boolean f10Momentary) { setFMomentary(10, f10Momentary); }

    public final boolean getF11Momentary() { return getFMomentary(11); }

    public final void setF11Momentary(boolean f11Momentary) { setFMomentary(11, f11Momentary); }

    public final boolean getF12Momentary() { return getFMomentary(12); }

    public final void setF12Momentary(boolean f12Momentary) { setFMomentary(12, f12Momentary); }

    public final boolean getF13Momentary() { return getFMomentary(13); }

    public final void setF13Momentary(boolean f13Momentary) { setFMomentary(13, f13Momentary); }

    public final boolean getF14Momentary() { return getFMomentary(14); }

    public final void setF14Momentary(boolean f14Momentary) { setFMomentary(14, f14Momentary); }

    public final boolean getF15Momentary() { return getFMomentary(15); }

    public final void setF15Momentary(boolean f15Momentary) { setFMomentary(15, f15Momentary); }

    public final boolean getF16Momentary() { return getFMomentary(16); }

    public final void setF16Momentary(boolean f16Momentary) { setFMomentary(16, f16Momentary); }

    public final boolean getF17Momentary() { return getFMomentary(17); }

    public final void setF17Momentary(boolean f17Momentary) { setFMomentary(17, f17Momentary); }

    public final boolean getF18Momentary() { return getFMomentary(18); }

    public final void setF18Momentary(boolean f18Momentary) { setFMomentary(18, f18Momentary); }

    public final boolean getF19Momentary() { return getFMomentary(19); }

    public final void setF19Momentary(boolean f19Momentary) { setFMomentary(19, f19Momentary); }

    public final boolean getF20Momentary() { return getFMomentary(20); }

    public final void setF20Momentary(boolean f20Momentary) { setFMomentary(20, f20Momentary); }

    public final boolean getF21Momentary() { return getFMomentary(21); }

    public final void setF21Momentary(boolean f21Momentary) { setFMomentary(21, f21Momentary); }

    public final boolean getF22Momentary() { return getFMomentary(22); }

    public final void setF22Momentary(boolean f22Momentary) { setFMomentary(22, f22Momentary); }

    public final boolean getF23Momentary() { return getFMomentary(23); }

    public final void setF23Momentary(boolean f23Momentary) { setFMomentary(23, f23Momentary); }

    public final boolean getF24Momentary() { return getFMomentary(24); }

    public final void setF24Momentary(boolean f24Momentary) { setFMomentary(24, f24Momentary); }

    public final boolean getF25Momentary() { return getFMomentary(25); }

    public final void setF25Momentary(boolean f25Momentary) { setFMomentary(25, f25Momentary); }

    public final boolean getF26Momentary() { return getFMomentary(26); }

    public final void setF26Momentary(boolean f26Momentary) { setFMomentary(26, f26Momentary); }

    public final boolean getF27Momentary() { return getFMomentary(27); }

    public final void setF27Momentary(boolean f27Momentary) { setFMomentary(27, f27Momentary); }

    public final boolean getF28Momentary() { return getFMomentary(28); }

    public final void setF28Momentary(boolean f28Momentary) { setFMomentary(28, f28Momentary); }

    /**
     * Locomotive address. The exact format is defined by the specific
     * implementation, as subclasses of LocoAddress will contain different
     * information.
     *
     * This is an unbound property.
     *
     * @return The locomotive address
     */
    public abstract LocoAddress getLocoAddress();

    // register for notification if any of the properties change
    public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener p);

    public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener p);

    public abstract Vector<java.beans.PropertyChangeListener> getListeners();

    /**
     * Not for general use, see {@link #release()} and {@link #dispatch()}.
     * <p>
     * Dispose of object when finished it. This does not free any hardware
     * resources used; rather, it just cleans up the software implementation.
     * <P>
     * Used for handling certain internal error conditions, where the object
     * still exists but hardware is not associated with it.
     * <P>
     * After this, further usage of this Throttle object will result in a
     * JmriException.
     *
     * @deprecated Calls to dispose of a throttle should now be made via the
     * throttle manager or by using {@link #dispose(ThrottleListener l)}.
     */
    @Deprecated
    public abstract void dispose();

    /**
     * Finished with this Throttle, tell the layout that the locomotive is
     * available for reuse/reallocation by somebody else.
     * <P>
     * After this, further usage of this Throttle object will result in a
     * JmriException. Do not call dispose after release.
     * <P>
     * Normally, release ends with a call to dispose.
     *
     * @deprecated Calls to dispose of a throttle should now be made via the
     * throttle manager or by using {@link #release(ThrottleListener l)}
     */
    @Deprecated
    public abstract void release();

    /**
     * Finished with this Throttle, tell the layout that the locomotive is
     * available for reuse/reallocation by somebody else. If possible, tell the
     * layout that this locomotive has been dispatched to another user. Not all
     * layouts will implement this, in which case it is synomous with release();
     * <P>
     * After this, further usage of this Throttle object will result in a
     * JmriException.
     * <P>
     * Normally, dispatch ends with a call to dispose.
     *
     * @deprecated Calls to dispose of a throttle should now be made via the
     * throttle manager, or by using {@link #dispatch(ThrottleListener l)}
     */
    @Deprecated
    public abstract void dispatch();

    /**
     * Not for general use, see {@link #release(ThrottleListener l)} and
     * {@link #dispatch(ThrottleListener l)}.
     * <p>
     * Dispose of object when finished it. This does not free any hardware
     * resources used; rather, it just cleans up the software implementation.
     * <P>
     * Used for handling certain internal error conditions, where the object
     * still exists but hardware is not associated with it.
     * <P>
     * After this, further usage of this Throttle object will result in a
     * JmriException.
     *
     * @param l {@link ThrottleListener} to dispose of
     */
    public abstract void dispose(ThrottleListener l);

    /**
     * Finished with this Throttle, tell the layout that the locomotive is
     * available for reuse/reallocation by somebody else.
     * <P>
     * After this, further usage of this Throttle object will result in a
     * JmriException. Do not call dispose after release.
     * <P>
     * Normally, release ends with a call to dispose.
     *
     * @param l {@link ThrottleListener} to release
     */
    public abstract void release(ThrottleListener l);

    /**
     * Finished with this Throttle, tell the layout that the locomotive is
     * available for reuse/reallocation by somebody else. If possible, tell the
     * layout that this locomotive has been dispatched to another user. Not all
     * layouts will implement this, in which case it is synomous with release();
     * <P>
     * After this, further usage of this Throttle object will result in a
     * JmriException.
     * <P>
     * Normally, dispatch ends with a call to dispose.
     *
     * @param l {@link ThrottleListener} to dispatch
     */
    public abstract void dispatch(ThrottleListener l);

    public abstract void setRosterEntry(BasicRosterEntry re);

    public abstract BasicRosterEntry getRosterEntry();
}
