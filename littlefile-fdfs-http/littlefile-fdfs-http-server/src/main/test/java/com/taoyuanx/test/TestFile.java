package com.taoyuanx.test;

import com.taoyuanx.littlefile.server.dto.ImageWH;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TestFile {
    @Test
    public void test() {
        String cutSize = "100*100";
        List<ImageWH> collect = Arrays.stream(cutSize.split(",")).map(singleSize -> {
            try {
                String size[] = singleSize.split("x");
                if (size.length != 2) {
                    return null;
                }
                int width = Integer.parseInt(size[0]);
                int height = Integer.parseInt(size[1]);
                return new ImageWH(width, height);
            } catch (Exception e) {
                System.out.println(singleSize);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        System.out.println(collect);
    }
}
