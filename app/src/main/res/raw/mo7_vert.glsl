uniform mat4 u_MVPMatrix;

attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord;

varying vec3 v_vert;
varying vec3 v_normal;
varying vec2 v_texCoord;

void main() {
    v_vert = a_position.xyz;
    v_normal = a_normal;
    v_texCoord = a_texCoord;

    gl_Position = u_MVPMatrix * a_position;
}
