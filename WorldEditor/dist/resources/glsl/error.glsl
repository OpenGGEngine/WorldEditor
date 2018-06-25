COMPILED GGSL ERROR SOURCE: From shader phongshadow.frag with error code 0: 0(153) : error C1101: ambiguous overloaded function reference "clamp(float, int, float)"
    (0) : gp5 float64_t clamp(float64_t, float64_t, float64_t)
    (0) : float clamp(float, float, float)
#version 420 core
#define LIGHTNUM 100
layout(location = 0) out vec4 fcolor;
in vertexData {
	vec2 textureCoord;
 vec3 pos;
 vec3 norm;
};
uniform mat4 view;
uniform mat4 model;
uniform mat4 perspective;
struct Light {
	vec4 lightpos;
 vec4 color;
 mat4 view;
 mat4 perspective;
	float lightdistance;
	float lightdistance2;
	float shadow;
	float padding1;
	vec4 padding2;
};
struct Material {
	bool hasnormmap;
 bool hasspecmap;
 bool hasspecpow;
 bool hasambmap;
	bool hascolormap;
 vec3 ks;
 vec3 ka;
 vec3 kd;
 float ns;
};
uniform sampler2D Kd;
uniform sampler2D Ka;
uniform sampler2D Ks;
uniform sampler2D Ns;
uniform sampler2D bump;
uniform samplerCube cubemap;
vec3 n;
vec3 ambient;
vec3 specular;
vec3 diffuse;
vec4 color;
float specpow;
vec3 reflectedcolor;
uniform float uvmultx;
uniform float uvmulty;
uniform vec3 camera;
float bloomMin = 0.9;
float vis = 1;
vec3 eyedir;
layout(std140) uniform LightBuffer {
	Light lights[LIGHTNUM];
};
uniform int numLights;
uniform sampler2D shadowmap;
uniform sampler2D shadowmap2;
uniform sampler2D shadowmap3;
uniform Material material;
float trans;
vec4 getTex(sampler2D tname){
	return texture(tname, textureCoord);
}
mat3 cotangent_frame(vec3 N, vec3 p, vec2 uv){
	// get edge vectors of the pixel triangle
 vec3 dp1 = dFdx( p );
 vec3 dp2 = dFdy( p );
 vec2 duv1 = dFdx( uv );
 vec2 duv2 = dFdy( uv );

 // solve the linear system
 vec3 dp2perp = cross( dp2, N );
 vec3 dp1perp = cross( N, dp1 );
 vec3 T = dp2perp * duv1.x + dp1perp * duv2.x;
 vec3 B = dp2perp * duv1.y + dp1perp * duv2.y;

 // construct a scale-invariant frame
 float invmax = inversesqrt( max( dot(T,T), dot(B,B) ) );
 return mat3( T * invmax, B * invmax, N );
}
vec3 calculatenormal(vec3 N, vec3 V, vec3 map, vec2 texcoord){
	map = map * 255./127. - 128./127.;
 mat3 TBN = cotangent_frame( N, -V, texcoord);
 return normalize( TBN * map );
}
void useMaterial(Material mat){
	//if(material.hascolormap){
		color = texture(Kd, textureCoord);
	//}else{
	//	color = vec4(material.kd, 1);
	//}

 diffuse = color.rgb;

 trans = color.a;

 ambient = 0.1f * diffuse;

 specular = vec3(1,1,1);

 specpow = 32;
 /*if(material.hasspecpow){
 vec4 specpowvec = getTex(Ns);
 specpow = specpowvec.r * 32;
 }else{
 specpow = material.ns;
 }*/

 if(mat.hasspecmap){
 specular = texture(Ks, textureCoord).rgb;
 }else{
 specular = material.ks;
 }

 if(mat.hasnormmap){
		n = calculatenormal(normalize((model * vec4(norm,0.0f)).xyz),
		 pos.xyz-camera, texture(bump, textureCoord).xyz, textureCoord);
 }else{
		n = normalize((model * vec4(norm,0.0f)).xyz);
	}

	//reflectedcolor = texture(cubemap, normalize(reflect(eyedir,n))).xyz;
}
void generatePhongData(){
	eyedir = normalize(camera - pos.xyz);
}
vec3 getPhongFrom(Light light){
	float distance = length( light.lightpos.xyz - pos.xyz );
	float attenuation = clamp((1.0 - (distance/light.lightdistance)), 0.0, 10.0);
	attenuation = attenuation * attenuation;

	vec3 lightDir = normalize(light.lightpos.xyz - pos.xyz);
	vec3 halfwayDir = normalize(lightDir + eyedir);

 float cosTheta = max(dot( n,lightDir ), 0.0f );
 vec3 fdif = diffuse * light.color.xyz * cosTheta * attenuation;
	
 float cosAlpha = clamp(max(dot(n, halfwayDir), 0.0), 0, 1);
	vec3 fspec = specular * light.color.xyz * pow(cosAlpha, specpow) * attenuation;
	
 vec3 fragColor = fdif + fspec;

 return fragColor;
}
float getShadowPercent(Light light, int i){
	vec4 lightspacePos = light.perspective*(light.view * vec4(pos, 1.0f));
 vec3 projCoords = lightspacePos.xyz;///lightspacePos.w;
 projCoords = projCoords * 0.5f + 0.5f;
 float closestDepth = texture(shadowmap, projCoords.xy).r;

 float bias = 0.005*tan(acos(dot(norm, light.lightpos.xyz))); // cosTheta is dot( n,l ), clamped between 0 and 1
 bias = clamp(bias, 0,0.01);
 bias = 0.008f;

 float shadow = (projCoords.z -0.5f) - bias > closestDepth ? 0.0f : 1.0f;
 return shadow;
}
void main(){
	generatePhongData();
 useMaterial(material);
 vec3 col = vec3(0,0,0);//ambient;
 int w = 0;
 //for(int i = 0; i < numLights; i++){
 col += getPhongFrom(lights[0]) * getShadowPercent(lights[0], 0);
 w++;
 //}
 
 //fcolor = vec4(col, trans);
 //fcolor = vec4(vec3(getShadowPercent(lights[0], 0)), trans);
 fcolor = vec4(col + ambient, trans);
}
