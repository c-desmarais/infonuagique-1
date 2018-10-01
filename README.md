# TP1 - Infonuagique

Afin d'exécuter ce TP, veuillez procéder avec les étapes suivantes:

**Envoyer le travail sur la machine virtuelle:**
```
scp -i sshkeys.pem 1762064-1721384-TP1-INF8480.tar.gz ubuntu@ip-flottante
```
Notez que l'ip flottante utilisee dans ce TP etait: 132.207.12.114. 
Vous pouvez la réutiliser.

Juste au cas, nous avons laissez le fichier sshkeys.pem donc le chargé peut l'utiliser pour les clés privées. Par contre, s’il a d’autre clés ssh, il peut simplement remplacer l’argument sshkeys.pem par un autre pour la clé prive.

**Installation du serveur sur la machine virtuelle:**
Se connecter à la machine virtuelle en effectuant la commande suivante: 

```
ssh -i sshkeys.pem ubuntu@ip-flottante
```
Si l'ip flottante est différente ici, il faut simplement la mettre en argument en remplaçant l’autre. 

Extraire le fichier envoyé sur la VM: 
```
tar xvzf 1762064-1721384-TP1-INF8480.tar.gz
```

Se rendre dans le dossier bin:
```
cd 1762064-1721384-TP1-INF8480/Partie2/ResponseTime_Analyzer/bin
```

Démarrer le registre RMI:
```
rmiregistry
```

Ouvrir un autre terminal, et se connecter avec la machine virtuelle (avec la même commande que précédemment):
```
ssh -i sshkeys.pem ubuntu@ip-flottante
```

Exécuter le serveur
```
  1762064-1721384-TP1-INF8480/Partie2/ResponseTime_Analyzer/server.sh
```

**Installation du client sur la machine locale:**
- Se rendre dans le dossier approprie:
```
cd 1762064-1721384-TP1-INF8480/Partie2/ResponseTime_Analyzer/
```

- Entrer les commandes du TP par exemple : 
```
./client.sh new testuser testmp

./client.sh list
```

Il est également possible d'installer plusieurs clients dans des dossiers différents afin de voir si le serveur est bien capable de répondre aux demandes de plus d'un client (ce qui est le cas).


Veuillez noter qu'il est préférable d'exécuter le client sur un ordinateur de l'école, soit un ordinateur utilisant JAVA 1.8.
