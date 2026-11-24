{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  packages = [
    pkgs.openjdk
    pkgs.gnumake
  ];

  shellHook = ''
    echo "☕ Ambiente Java (JDK + Make) pronto para o G1_ProjetoComercioEletronico!"
    echo "Comandos:"
    echo "  make test    # Roda os 63 testes automatizados"
    echo "  make demo    # Roda o cenario CA-01 no terminal"
    echo "  make run     # Sobe o servidor HTTP na porta 8080"
  '';
}
