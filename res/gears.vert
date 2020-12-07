#if __VERSION__ < 130
    attribute vec4 aPosition;
    attribute vec2 aTexCoord;
    varying vec2 vTexCoord;
#else
    #if __VERSION__ < 330
        in vec4 aPosition;
        in vec2 aTexCoord;
    #else
        layout(location = 0) in vec4 aPosition;
        layout(location = 1) in vec2 aTexCoord;
    #endif
                 
    out vec2 vTexCoord;
#endif
                 
void main() {
    vTexCoord = aTexCoord;
    gl_Position = aPosition;
}