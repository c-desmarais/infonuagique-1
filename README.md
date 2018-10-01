# TP1 - Infonuagique

**Envoyer le travail sur la machine virtuelle:**
```
 scp -i sshkeys.pem 1762064-1721384-TP1-INF8480.tar.gz ubuntu@ip-flottante
```
Notez que l'ip flottante utilisée dans ce TP était: 132.207.12.114. 
Vous pouvez la réutiliser.

Juste au cas, nous avons laissez le fichier sshkeys.pem donc le chargé
peut l'utiliser pour les cles privées. Par contre, si le chargé utilise d'autres clés ssh, il peut simplement remplacer l'argument sshkeys.pem par un autre pour la clé privée.

# Partie 1

Veuillez noter que vous trouverez le rapport écrit pour la partie 1 dans /Partie1/Rapport-Partie1.docx.

**Lancer le serveur distant sur la machine virtuelle:**
Se connecter a la machine virtuelle en effectuant la commande suivante: 

```
 ssh -i sshkeys.pem ubuntu@ip-flottante
```
Si l'ip flotante est différente ici, il faut simplement la mettre en argument en remplacant l'autre. 

Décompresser le fichier envoyé sur la VM: 
```
 tar xvzf 1762064-1721384-TP1-INF8480.tar.gz
```

Se rendre dans le dossier bin:
```
 cd 1762064-1721384-TP1-INF8480/Partie1/ResponseTime_Analyzer/bin
```

Démarrer le registre RMI:
```
 rmiregistry
```

Ouvrir un autre terminal et se connecter avec la machine virtuelle (avec la meme commande que precedemment):
```
 ssh -i sshkeys.pem ubuntu@ip-flottante
```

Exécuter le serveur:
```
  1762064-1721384-TP1-INF8480/Partie1/ResponseTime_Analyzer/server.sh
```


##Lancer le serveur local sur la machine du laboratoire:**
Proceder avec le dezippage du fichier sur la machine locale, ainsi que le demarrage du registre RMI
ainsi que l'execution du serveur (memes commandes que precedemment)

##Lancer le client sur la machine du laboratoire:**
```
  1762064-1721384-TP1-INF8480/Partie1/ResponseTime_Analyzer/client.sh
```

# Partie 2
Afin d'executer la partie 2 de ce TP, veuillez procéder avec les étapes suivantes:

**Installation du serveur sur la machine virtuelle:**
Se connecter à la machine virtuelle en effectuant la commande suivante: 

A partir de la machine virtuelle, se rendre dans le dossier bin:
```
 cd 1762064-1721384-TP1-INF8480/Partie2/ResponseTime_Analyzer/bin
```

Démarrer le registre RMI:
```
 rmiregistry
```

Ouvrir un autre terminal et se connecter avec la machine virtuelle (avec la meme commande que precedemment):
```
 ssh -i sshkeys.pem ubuntu@ip-flottante
```

Executer le serveur
```
  1762064-1721384-TP1-INF8480/Partie2/ResponseTime_Analyzer/server.sh
```

**Installation du client sur la machine locale:**
- Se rendre dans le dossier approprié:
```
 cd 1762064-1721384-TP1-INF8480/Partie2/ResponseTime_Analyzer/
```

- Entrer les commandes du TP par exemple : 
```
 ./client.sh new testuser testmp

 ./client.sh list
```

Il est également possible d'installer plusieurs clients dans des dossiers differents afin de voir si le serveur est bien capable de répondre aux demandes de plus d'un client (ce qui est le cas).


Veuillez noter qu'il est preferable d'exécuter le client sur un ordinateur de l'ecole, soit un ordinateur
utilisant Java 1.8. 