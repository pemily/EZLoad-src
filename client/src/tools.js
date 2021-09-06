 export function getChromeVersion(){
    var pieces = navigator.userAgent.match(/Chrom(?:e|ium)\/([0-9]+)\.([0-9]+)\.([0-9]+)\.([0-9]+)/);
    if (pieces == null || pieces.length !== 5) {
        return undefined;
    }
    pieces = pieces.map(piece => parseInt(piece, 10));
    const fullVersion = pieces[1]+"."+ pieces[2]+"."+ pieces[3]+ "."+ pieces[4];
    var version = pieces[1]+"."+ pieces[2]+"."+ pieces[3];
    return { fullVersion, version };
}



