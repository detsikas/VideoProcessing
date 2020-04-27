#version 300 es

in vec4 a_Position;
in vec4 a_TexCoord;
uniform mat4 uTexMatrix;

out vec2 TexCoord;

void main() {
    gl_Position = a_Position;
    TexCoord = (uTexMatrix*a_TexCoord).xy;
}