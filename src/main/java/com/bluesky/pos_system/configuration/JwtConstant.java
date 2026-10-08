package com.bluesky.pos_system.configuration;

public class JwtConstant {
    public static final String JWT_SECRET = (System.getenv("JWT_SECRET") != null && !System.getenv("JWT_SECRET").isBlank())
            ? System.getenv("JWT_SECRET")
            : "zntuceyczbzgazycopqqwvryithxbewcymywpyqcehlpgxzwowgmqsmizmvremjncvqxqrnjwoidanptqzxomilyhmeeclqobxjipuxzegyfrqxevrekglqkjgnhgnsmhaqofiycmqqgrpcnznebbyvajqclvqzw";

    public static final String JWT_HEADER = "Authorization";
}
