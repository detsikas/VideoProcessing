#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require

precision mediump float;
uniform samplerExternalOES sTexture;
in vec2 TexCoord; // the camera bg texture coordinates
out vec4 FragColor;

void main() {
    FragColor = vec4(texture(sTexture, TexCoord));
}
