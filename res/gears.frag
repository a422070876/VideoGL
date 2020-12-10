uniform sampler2D sTexture;

#if __VERSION__ < 130
    varying vec2 vTexCoord;

    #define out_Color gl_FragColor
#else
    in vec2 vTexCoord;

    #if __VERSION__ < 330
        out vec4 out_Color;
    #else
        layout(location = 0) out vec4 out_Color;
    #endif
#endif

void main() {
    vec4 rgba = texture(sTexture ,  vec2(vTexCoord.x , 1.0 - vTexCoord.y));
    out_Color = rgba;
}
