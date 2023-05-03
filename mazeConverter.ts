function convertToJavaMatrix(input: string): string {
  const rows = input.trim().split('\n');
  const matrix = rows.map((row) =>
    row.trim().split('').map((cell) => parseInt(cell))
  );
  const javaArray = matrix.map(
    (row) => `{${row.map((cell) => `${cell}`).join(',')}}`
  );
  return `int[][] maze = {${javaArray.join(',')}};`;
}

const input = `0001111111
0000000001
1001111001
1000001001
1001111111
1001001001
1001001001
1000001001
1001001001
100100000
111111100`;
const javaMatrix = convertToJavaMatrix(input);
console.log(javaMatrix);