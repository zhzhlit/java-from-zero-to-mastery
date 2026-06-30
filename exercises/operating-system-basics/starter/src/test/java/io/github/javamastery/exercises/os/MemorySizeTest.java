package io.github.javamastery.exercises.os;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MemorySize")
class MemorySizeTest {
    @Test
    @Disabled("练习：移除 @Disabled，实现二进制内存单位换算")
    @DisplayName("按二进制单位换算字节、KiB 和 MiB")
    void convertsBinaryMemoryUnits() {
        assertEquals(2_048, MemorySize.ofKiB(2).bytes());
        assertEquals(3_072, MemorySize.ofMiB(3).toKiB());
        assertEquals(1_024, MemorySize.parse("1 GiB").toMiB());
    }

    @Test
    @Disabled("练习：移除 @Disabled，解析带单位的内存文本")
    @DisplayName("解析带单位的内存文本")
    void parsesMemoryText() {
        assertEquals(512, MemorySize.parse("512B").bytes());
        assertEquals(2_097_152, MemorySize.parse("2 MiB").bytes());
    }

    @Test
    @Disabled("练习：移除 @Disabled，判断内存是否不超过限制")
    @DisplayName("判断内存是否不超过限制")
    void checksWhetherMemoryFitsLimit() {
        assertTrue(MemorySize.ofMiB(256).fitsWithin(MemorySize.ofMiB(512)));
        assertFalse(MemorySize.ofMiB(1024).fitsWithin(MemorySize.ofMiB(512)));
    }

    @Test
    @Disabled("练习：移除 @Disabled，拒绝无效内存值")
    @DisplayName("拒绝负数、空文本和未知单位")
    void rejectsInvalidMemoryValues() {
        assertThrows(IllegalArgumentException.class, () -> MemorySize.ofBytes(-1));
        assertThrows(IllegalArgumentException.class, () -> MemorySize.parse(""));
        assertThrows(IllegalArgumentException.class, () -> MemorySize.parse("2MB"));
    }
}
