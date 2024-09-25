{pkgs, ...}: {
  enterShell = ''
    hello
    versions
  '';

  enterTest = ''
    echo "Running tests"
    git --version | grep --color=auto "${pkgs.git.version}"
  '';

  env.GREET = "devenv";
  # env.JVM_OPTS = "-Dclojure.main.report=stderr";

  languages = {
    clojure.enable = true;
    nix.enable = true;
  };

  packages = with pkgs; [
    babashka
    git
    neil
  ];

  pre-commit.hooks = {
    alejandra.enable = true;
    cljfmt.enable = true;
    deadnix.enable = true;
    shellcheck.enable = true;
    statix.enable = true;
  };

  scripts = {
    hello.exec = ''
      echo hello from $GREET
    '';
    versions.exec = ''
      echo "=== Versions ==="
      bb --version
      git --version
      neil --version
      echo "=== === ==="
    '';
  };
}
