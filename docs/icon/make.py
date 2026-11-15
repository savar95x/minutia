# Minutia app icon: a geometric "m:" mark on the 108x108 adaptive-icon canvas.
# The same numbers are hard-coded in app/src/main/res/drawable/ic_launcher_{foreground,monochrome}.xml;
# change both together. Previews (circle/squircle masks, themed icon, 48px) are written to the current dir.
# Usage: pip install cairosvg pillow && python3 docs/icon/make.py
import cairosvg, io
from PIL import Image, ImageDraw
BG="#121614"; CREAM="#F0ECDE"; CORAL="#FF8A65"
SW=8.2            # stroke width
dx=1.5            # optical centring of the whole mark
def m_path(dx=dx):
    x1,x2,x3=31+dx,46+dx,61+dx; yb=66.5; ya=51.5; r=7.5
    return (f"M{x1},{yb} V{ya} A{r},{r} 0 0 1 {x2},{ya} V{yb} "
            f"M{x2},{ya} A{r},{r} 0 0 1 {x3},{ya} V{yb}")
dots=[(73.5+dx,50.5),(73.5+dx,62.5)]; DR=3.5
def svg(bg=True, mono=False):
    fg = "#000" if mono else CREAM; acc = "#000" if mono else CORAL
    b = f'<rect width="108" height="108" fill="{BG}"/>' if bg else ""
    c = "".join(f'<circle cx="{x}" cy="{y}" r="{DR}" fill="{acc}"/>' for x,y in dots)
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="864" height="864">{b}'
            f'<path d="{m_path()}" fill="none" stroke="{fg}" stroke-width="{SW}" stroke-linecap="round" stroke-linejoin="round"/>{c}</svg>')
def png(s): return Image.open(io.BytesIO(cairosvg.svg2png(bytestring=s.encode()))).convert("RGBA")
full=png(svg())
full.save("full.png")
def mask(img,kind,size):
    W=img.width; a,b=int(W*18/108),int(W*90/108)
    m=Image.new("L",(W,W),0); d=ImageDraw.Draw(m)
    if kind=="circle": d.ellipse((a,a,b,b),fill=255)
    else: d.rounded_rectangle((a,a,b,b),int(W*26/108),fill=255)
    o=Image.new("RGBA",(W,W),(0,0,0,0)); o.paste(img,(0,0),m)
    return o.crop((a,a,b,b)).resize((size,size),Image.LANCZOS)
# themed (monochrome) preview: Android tints the silhouette
mono=png(svg(bg=False,mono=True)); r,g,b_,a=mono.split()
themed=Image.new("RGBA",mono.size,(42,52,36,255)); fgc=Image.new("RGBA",mono.size,(200,222,180,255)); themed.paste(fgc,(0,0),a)
sheet=Image.new("RGB",(900,260),(34,34,38)); x=20
for img in (full,themed):
    for kind in ("circle","squircle"):
        big=mask(img,kind,180); sheet.paste(big,(x,20),big); x+=200
small=mask(full,"squircle",48); sheet.paste(small,(830,70),small)
small2=mask(full,"circle",48); sheet.paste(small2,(830,140),small2)
sheet.save("final_preview.png")
# 512px store-style PNG for README
mask(full,"squircle",512).save("icon_512.png")
