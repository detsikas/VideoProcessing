#version 300 es

precision mediump float;
uniform sampler2D sTexture;
in vec2 TexCoord; // the camera bg texture coordinates
out vec4 FragColor;

void main() {
    FragColor = vec4(texture(sTexture, TexCoord));
}
