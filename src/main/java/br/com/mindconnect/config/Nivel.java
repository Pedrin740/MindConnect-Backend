package br.com.mindconnect.config;

// Níveis de acesso que uma rota pode exigir
public enum Nivel {
    PUBLICO,                       // qualquer pessoa (cadastro, login e consultas do catálogo)
    LOGADO,                        // qualquer usuário autenticado
    ADMINISTRADOR,                 // somente administradores
    PROPRIETARIO,                  // somente o dono dos dados (o {id} da URL deve ser o do usuário logado)
    PROPRIETARIO_OU_ADMINISTRADOR  // o dono dos dados ou um administrador
}
