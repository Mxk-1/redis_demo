package com.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * @author MXK
 * @version 1.0
 * @description TODO
 * @date 2023/3/2 17:40
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String name;
    private int age;

}

    