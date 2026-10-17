import ExcelJS from 'exceljs'

type NivelPlantilla = 'NIVEL_2' | 'NIVEL_3'

const COLUMNAS_NIVEL_2 = [
  'apellido paterno',
  'apellido materno',
  'nombres',
  'DNI',
  'celular',
  'correo',
  'cargo',
]

const COLUMNAS_NIVEL_3 = [
  'ranking',
  'apellido paterno',
  'apellido materno',
  'nombres',
  'DNI',
  'correo',
  'celular',
  'cargo',
  'situacion',
]

function aplicarEstiloEncabezado(fila: ExcelJS.Row): void {
  fila.height = 28

  fila.eachCell((celda) => {
    celda.font = {
      bold: true,
      color: { argb: 'FFFFFFFF' },
      size: 11,
    }

    celda.fill = {
      type: 'pattern',
      pattern: 'solid',
      fgColor: { argb: 'FF17665E' },
    }

    celda.alignment = {
      horizontal: 'center',
      vertical: 'middle',
      wrapText: true,
    }

    celda.border = {
      bottom: {
        style: 'thin',
        color: { argb: 'FF123F3B' },
      },
    }
  })
}

function configurarHojaDatos(hoja: ExcelJS.Worksheet, columnas: string[]): void {
  hoja.columns = columnas.map((nombre) => ({
    header: nombre,
    key: nombre,
    width: Math.max(nombre.length + 5, 18),
  }))

  aplicarEstiloEncabezado(hoja.getRow(1))

  hoja.views = [
    {
      state: 'frozen',
      ySplit: 1,
    },
  ]

  hoja.autoFilter = {
    from: {
      row: 1,
      column: 1,
    },
    to: {
      row: 1,
      column: columnas.length,
    },
  }
}

function agregarHojaInstrucciones(libro: ExcelJS.Workbook, nivel: NivelPlantilla): void {
  const hoja = libro.addWorksheet('Instrucciones')

  hoja.columns = [{ width: 25 }, { width: 95 }]

  hoja.addRow(['CAMPO', 'INSTRUCCIÓN'])

  const instrucciones: [string, string][] = [
    [
      'Formato',
      'Completa los datos en la hoja Datos. No cambies los nombres ni el orden de las columnas.',
    ],
    ['DNI', 'Debe contener exactamente 8 dígitos. No ingreses espacios ni guiones.'],
    [
      'Apellidos y nombres',
      'Son campos obligatorios. Escribe los datos de la persona correspondiente.',
    ],
    ['Celular', 'Es un campo opcional. Si lo completas, utiliza un número válido.'],
    ['Correo', 'Es un campo opcional. Si lo completas, utiliza una dirección de correo válida.'],
    [
      'Cargo',
      nivel === 'NIVEL_2'
        ? 'Valores permitidos: CAL, CAE o CTL.'
        : 'Valores permitidos: APLICADOR, ORIENTADOR u OPERADOR TECNOLOGICO.',
    ],
    ['Filas', 'Cada fila debe corresponder a una persona. No repitas el DNI dentro del archivo.'],
    [
      'Ejemplo',
      'La primera fila de datos es ilustrativa. Elimínala antes de importar y reemplázala por los datos reales.',
    ],
    [
      'Validación',
      'Si existen errores en las filas, corrígelos antes de volver a importar el archivo.',
    ],
  ]

  if (nivel === 'NIVEL_3') {
    instrucciones.splice(
      1,
      0,
      ['Ranking', 'Debe ser un número entero positivo.'],
      ['Situación', 'Valores permitidos: TITULAR o RESERVA.'],
    )
  }

  instrucciones.forEach((instruccion) => {
    hoja.addRow(instruccion)
  })

  aplicarEstiloEncabezado(hoja.getRow(1))

  hoja.eachRow((fila, numeroFila) => {
    if (numeroFila > 1) {
      fila.alignment = {
        vertical: 'middle',
        wrapText: true,
      }

      fila.height = 34
    }
  })

  hoja.views = [
    {
      state: 'frozen',
      ySplit: 1,
    },
  ]
}

function agregarFilaEjemplo(hoja: ExcelJS.Worksheet, nivel: NivelPlantilla): void {
  if (nivel === 'NIVEL_2') {
    hoja.addRow({
      'apellido paterno': 'EJEMPLO',
      'apellido materno': 'DEMO',
      nombres: 'PERSONA DEMOSTRACION',
      DNI: '12345678',
      celular: '999999999',
      correo: 'ejemplo@correo.com',
      cargo: 'CAL',
    })
  } else {
    hoja.addRow({
      ranking: 1,
      'apellido paterno': 'EJEMPLO',
      'apellido materno': 'DEMO',
      nombres: 'PERSONA DEMOSTRACION',
      DNI: '12345678',
      correo: 'ejemplo@correo.com',
      celular: '999999999',
      cargo: 'APLICADOR',
      situacion: 'TITULAR',
    })
  }

  const filaEjemplo = hoja.getRow(2)

  filaEjemplo.eachCell((celda) => {
    celda.font = {
      italic: true,
      color: { argb: 'FF64748B' },
    }

    celda.fill = {
      type: 'pattern',
      pattern: 'solid',
      fgColor: { argb: 'FFF1F5F9' },
    }
  })

  // Evita que Excel interprete el DNI como número.
  const indiceDni = nivel === 'NIVEL_2' ? 4 : 5
  hoja.getRow(2).getCell(indiceDni).numFmt = '@'
}

export async function descargarPlantillaExcel(nivel: NivelPlantilla): Promise<void> {
  const libro = new ExcelJS.Workbook()

  libro.creator = 'eda'
  libro.subject = `Plantilla de importación ${nivel}`
  libro.title = `Plantilla ${nivel}`
  libro.created = new Date()

  const columnas = nivel === 'NIVEL_2' ? COLUMNAS_NIVEL_2 : COLUMNAS_NIVEL_3

  const hojaDatos = libro.addWorksheet('Datos')

  configurarHojaDatos(hojaDatos, columnas)
  agregarFilaEjemplo(hojaDatos, nivel)
  agregarHojaInstrucciones(libro, nivel)

  const contenido = await libro.xlsx.writeBuffer()

  const archivo = new Blob([contenido], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  })

  const url = URL.createObjectURL(archivo)
  const enlace = document.createElement('a')

  enlace.href = url
  enlace.download = `plantilla_${nivel.toLowerCase()}.xlsx`

  document.body.appendChild(enlace)
  enlace.click()
  enlace.remove()

  URL.revokeObjectURL(url)
}
