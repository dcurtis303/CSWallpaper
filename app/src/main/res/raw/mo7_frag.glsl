precision mediump float;

uniform mat4 u_MMatrix;
uniform vec3 cameraPosition;

uniform sampler2D s_texture;
uniform float materialShininess;
uniform vec3 materialSpecularColor;

const int NUM_LIGHTS = 4;

uniform struct Light {
	vec3 position;
	vec3 color;
	float attenuation;
	float ambientCoeff;
} light[NUM_LIGHTS];

varying vec3 v_vert;
varying vec3 v_normal;
varying vec2 v_texCoord;

vec3 normal;
vec3 surfacePos;
vec4 surfaceColor;

vec3 addLight(int index) {
    vec3 surfaceToLight = normalize(light[index].position - surfacePos);
    vec3 surfaceToCamera = normalize(cameraPosition - surfacePos);

    //ambient
    vec3 ambient = light[index].ambientCoeff * (surfaceColor.rgb * light[index].color);

    //diffuse
	float diffuseCoefficient = max(0.0, dot(normal, surfaceToLight));
	vec3 diffuse = diffuseCoefficient * surfaceColor.rgb * light[index].color;

    //specular
    float specularCoefficient = 0.0;
    if(diffuseCoefficient > 0.0)
        specularCoefficient = pow(max(0.0, dot(surfaceToCamera, reflect(-surfaceToLight, normal))), materialShininess);
    vec3 specular = specularCoefficient * materialSpecularColor * light[index].color;

    //attenuation
    float distanceToLight = length(light[index].position - surfacePos);
    float attenuation = 1.0 / (1.0 + light[index].attenuation * pow(distanceToLight, 2.0));

    //linear color (color before gamma correction)
    vec3 linearColor = ambient + attenuation*(diffuse + specular);
    
    return linearColor;
}

void main() {
    //vec3 normal = normalize(transpose(inverse(mat3(u_MMatrix))) * v_normal);
    normal = normalize(v_normal);
    surfacePos = vec3(u_MMatrix * vec4(v_vert, 1));
    surfaceColor = texture2D(s_texture, v_texCoord);

    vec3 linearColor_total = vec3(0,0,0);
    for (int i = 0; i < NUM_LIGHTS; i++) {
        linearColor_total += addLight(i);
    }

    //final color (after gamma correction)
    vec3 gamma = vec3(1.0/1.6);
    gl_FragColor = vec4(pow(linearColor_total, gamma), surfaceColor.a);
}
