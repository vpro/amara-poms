1. Redactie plaatst bron-uitzending op collectie in POMS (bijv. "NetInNL - te vertalen")
2. Deze wordt opgepakt door Amara Publisher
- mp4 file wordt van de uitzending losgetrokken en op download.omroep.nl geplaatst (vanuit amara is dat
vrij uit te spelen)
- item wordt aangemaakt in amara, met Nederlandse meta-data
- ondertitels van http://e.omroep.nl/tt888/<prid> worden toegevoegd en als die niet aanwezig is wordt er nog
gekeken alternatieve locatie (voor clips in poms) files.vpro.nl/netinnederland/subtitles/nl/
- wordt uit "NetInNl - te vertalen" naar "NetInNL" playlist geplaatst (op test http://poms-test.omroep.nl/#/edit/
POMS_S_VPRO_1414788)
3. Nadat ondertiteling is goedgekeurd pakt POMS Publisher het op:
- Deze maakt nieuwe clip aan voor de desbetreffende taal aan
- in een relatie wordt het verband gelegd met bron-uitzending
- Ondertiteling bestand wordt neergezet (voorlopig in /home/omroep/vpro_admin/files.vpro.nl/pages/
netinnederland/subtitles)
- Plaatst clip op Collectie (bijvoorbeeld "NetInNL Arabisch")
- Past bron aan zodat deze refereert aan de bron-uitzending: mid://omroep.nl/program/<mid>
- let op: ondertitels kunnen later ook nog aangepast worden. In dat geval niet naar de collectie verplaatsen
4. De website pakt de nieuwe clip automatisch op.
AMARA
Publisher
Mp4
bestand
met referer
beveiliging
mp4 url
vtt NL
Clip
<MID3>
<taal 2>
<webvtt 2>
Missend:
- webvtt nu op download, straks in POMS?
- hoe gaat de omroepspeler hier uiteindelijk mee om?