#version 120

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

uniform float Phosphor;

varying vec2 texCoord;

void main() {
    vec4 current = texture2D(DiffuseSampler, texCoord);
    vec4 previous = texture2D(PrevSampler, texCoord);

    gl_FragColor = mix(current, previous, clamp(Phosphor, 0.0, 1.0));
}
