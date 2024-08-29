#version 120

attribute vec3 Position;
attribute vec2 UV0;

varying vec2 vUV;

void main() {
    vUV = UV0;
    gl_Position = gl_ModelViewProjectionMatrix * vec4(Position, 1.0);
}
