const fs = require('fs');
const PDFParser = require('pdf2json');

const pdfParser = new PDFParser(null, 1);

pdfParser.on('pdfParser_dataReady', (pdfData) => {
    let allText = '';
    for (let i = 0; i < pdfData.Pages.length; i++) {
        const page = pdfData.Pages[i];
        let pageText = '';
        let lastY = -1;
        const sortedTexts = page.Texts.sort((a, b) => a.y - b.y || a.x - b.x);
        for (const text of sortedTexts) {
            if (lastY >= 0 && Math.abs(text.y - lastY) > 1) {
                pageText += '\n';
            }
            for (const r of text.R) {
                pageText += decodeURIComponent(r.T);
            }
            lastY = text.y;
        }
        allText += `\n--- Page ${i + 1} ---\n` + pageText;
    }
    fs.writeFileSync('d:\\MyProject\\springboot_vue_wms\\wms-web\\pdf_content.txt', allText, 'utf8');
    console.log('Done, length:', allText.length);
});

pdfParser.on('pdfParser_dataError', (err) => {
    console.error('Error:', err);
});

pdfParser.loadPDF('d:\\MyProject\\springboot_vue_wms\\wms-web\\基于SpringBoot的智能创作系统的设计与实现模板.pdf');