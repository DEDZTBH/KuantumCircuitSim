package matrix

import org.ejml.UtilEjml
import org.ejml.data.MatrixSparse
import org.ejml.data.MatrixType
import org.ejml.ops.MatrixIO
import java.io.*
import java.text.DecimalFormat

/**
 * Created by DEDZTBH on 2020/09/27.
 * Project KuantumCircuitSim
 */

object CMatrixIO {
    fun getMatrixType(mat: CMatrix): String {
        return if (mat.type == MatrixType.UNSPECIFIED) {
            mat.javaClass.simpleName
        } else {
            mat.type.name
        }
    }

    fun printTypeSize(out: PrintStream, mat: CMatrix) {
        if (mat is MatrixSparse) {
            val m = mat as MatrixSparse
            out.println(
                "Type = " + getMatrixType(mat) + " , rows = " + mat.getNumRows() +
                        " , cols = " + mat.getNumCols() + " , nz_length = " + m.nonZeroLength
            )
        } else {
            out.println("Type = " + getMatrixType(mat) + " , rows = " + mat.getNumRows() + " , cols = " + mat.getNumCols())
        }
    }

    fun padSpace(builder: java.lang.StringBuilder, length: Int): String {
        builder.delete(0, builder.length)
        for (i in 0 until length) {
            builder.append(' ')
        }
        return builder.toString()
    }

    @Throws(IOException::class)
    fun saveBin(A: CMatrix, fileName: String) {
        val fileStream = FileOutputStream(fileName)
        val stream = ObjectOutputStream(fileStream)
        try {
            stream.writeObject(A)
            stream.flush()
        } finally {
            // clean up
            fileStream.use {
                stream.close()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class)
    fun <T : CMatrix> loadBin(fileName: String): T {
        val fileStream = FileInputStream(fileName)
        val stream = ObjectInputStream(fileStream)
        val ret: T
        try {
            ret = stream.readObject() as T
            if (stream.available() != 0) {
                throw RuntimeException("File not completely read?")
            }
        } catch (e: ClassNotFoundException) {
            throw RuntimeException(e)
        }
        stream.close()
        return ret
    }

    fun CMatrix.printFancy2(
        out: PrintStream = System.out,
        length: Int = MatrixIO.DEFAULT_LENGTH,
        allssr: List<String>
    ) = let { mat ->
        printTypeSize(out, mat)
        val format = DecimalFormat("#")
        val builder = StringBuilder(length)
        val cols = mat.numCols
        val c = CNumber()
        var i = 0
        for (y in 0 until mat.numRows) {
            for (x in 0 until cols) {
                mat[y, x, c]
                var real = UtilEjml.fancyString(c.real, format, length, 4)
                var img = UtilEjml.fancyString(c.imaginary, format, length, 4)
                real += padSpace(builder, length - real.length)
                img = img + "i" + padSpace(builder, length - img.length)
                out.print("${allssr[i++]}: $real + $img")
                if (x < mat.numCols - 1) {
                    out.print(" , ")
                }
            }
            out.println()
        }
    }

}