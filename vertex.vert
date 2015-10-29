#version 130

in vec2 position;
in vec2 texcoords;

uniform mat4 mvMat;

out vec2 tex_coord;

void main()
{
    gl_Position = vec4(position, 0.0, 1.0);
    tex_coord = texcoords;
}