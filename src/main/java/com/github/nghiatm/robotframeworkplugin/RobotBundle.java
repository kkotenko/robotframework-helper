package com.github.nghiatm.robotframeworkplugin;

import com.intellij.AbstractBundle;
import com.intellij.reference.SoftReference;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.util.ResourceBundle;

/**
 * @author mrubino
 */
public class RobotBundle {

    // based off python's

    @NonNls
    private static final String BUNDLE = "messages.RobotBundle";

    private static Reference<ResourceBundle> instance;

    private RobotBundle() {
    }

    public static String message(@NonNls @PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return AbstractBundle.message(getBundle(), key, params);
    }

    // Cached loading
    private static ResourceBundle getBundle() {
        ResourceBundle bundle = SoftReference.dereference(instance);
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE);
            instance = new SoftReference<ResourceBundle>(bundle);
        }
        return bundle;
    }
}
