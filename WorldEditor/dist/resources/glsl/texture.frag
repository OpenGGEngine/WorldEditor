#version 410 core

layout(location = 0) out vec4 fcolor;

in vertexData{
    vec4 vertexColor;
    vec2 textureCoord;
    vec4 pos;
    vec3 norm;
};

uniform sampler2D Kd;

void main() {   
    fcolor = texture(Kd, textureCoord);
}