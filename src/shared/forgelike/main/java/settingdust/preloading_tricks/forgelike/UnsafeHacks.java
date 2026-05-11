/*
 * Minecraft Forge
 * Copyright (c) 2016-2019.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package settingdust.preloading_tricks.forgelike;

import java.lang.reflect.Field;

@SuppressWarnings("restriction")
public class UnsafeHacks {
    @SuppressWarnings("unchecked")
    public static <T> T getField(Field field, Object object) {
        final long l = JavaBypass.UNSAFE.objectFieldOffset(field);
        return (T) JavaBypass.UNSAFE.getObject(object, l);
    }

    public static void setField(Field data, Object object, Object value) {
        long offset = JavaBypass.UNSAFE.objectFieldOffset(data);
        JavaBypass.UNSAFE.putObject(object, offset, value);
    }
}
